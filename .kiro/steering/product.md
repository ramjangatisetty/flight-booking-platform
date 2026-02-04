---
inclusion: always
---

# Product Domain: Flight Booking Platform

Event-driven microservices platform for airline seat reservations using saga pattern for distributed transactions.

## Domain Services

### Booking Service (Port 8081)
- **Responsibility**: Booking lifecycle management and customer-facing API
- **Owns**: Booking aggregate (bookingId, flightId, passengerId, seatClass, status, price)
- **Exposes**: REST API for booking creation and status queries
- **Publishes** (actual topics from BookingEventPublisher):
  - `inventory.reserve.requested.v1` - When booking is created (starts saga)
  - `inventory.release.requested.v1` - When inventory needs to be released
  - `payment.requested.v1` - When payment is requested
  - `booking.confirmed.v1` - When booking saga completes successfully
  - `booking.rejected.v1` - When booking is rejected
- **Consumes**: `inventory.reserved.v1`, `inventory.rejected.v1`, `payment.succeeded.v1`, `payment.failed.v1`

### Inventory Service (Port 8082)
- **Responsibility**: Seat availability and reservation management
- **Owns**: Inventory items and reservations (flightId, seatClass, available/reserved counts)
- **Exposes**: Admin API for inventory management, query API for availability
- **Publishes**: `inventory.reserved.v1`, `inventory.rejected.v1`, `inventory.released.v1`
- **Consumes**: `inventory.reserve.requested.v1`, `inventory.release.requested.v1`

### Payment Service (Port 8083)
- **Responsibility**: Payment processing simulation
- **Publishes**: `payment.succeeded.v1`, `payment.failed.v1`
- **Consumes**: `payment.requested.v1`

## Saga Orchestration Pattern

The booking workflow implements a choreography-based saga:

```
CREATE BOOKING → RESERVE INVENTORY → PROCESS PAYMENT → CONFIRM/REJECT
     ↓                    ↓                  ↓              ↓
inventory.reserve   inventory.reserved  payment.*    booking.confirmed
  .requested.v1          .v1                           /rejected.v1
                         ↓
                  inventory.rejected.v1 → REJECTED
```

### Happy Path
1. User POST `/bookings` → Booking created with PENDING_PAYMENT status
2. `inventory.reserve.requested.v1` published with correlationId (NOT booking.created.v1)
3. Inventory service reserves seat → `inventory.reserved.v1`
4. Booking service requests payment → `payment.requested.v1`
5. Payment service processes → `payment.succeeded.v1`
6. Booking service publishes → `booking.confirmed.v1`
7. Booking status → CONFIRMED

### Failure Scenarios
- **Insufficient inventory**: `inventory.rejected.v1` → `booking.rejected.v1` → Booking status REJECTED
- **Payment failure**: `payment.failed.v1` → `inventory.release.requested.v1` → `booking.rejected.v1` → Booking status REJECTED

## Event Design Principles

### Event Envelope Structure (Actual from EventEnvelope.java)
All events use `EventEnvelope<T>` wrapper with nested structure:
```json
{
  "meta": {
    "eventId": "uuid",
    "eventType": "inventory.reserve.requested",
    "eventVersion": 1,
    "occurredAt": "2026-02-03T19:00:00Z",
    "correlationId": "uuid",
    "producer": "booking-service"
  },
  "data": { ... event-specific payload ... }
}
```

**IMPORTANT:** The structure uses:
- `meta.eventId` (NOT `eventId` at root)
- `meta.eventType` (NOT `eventType` at root)
- `meta.correlationId` (NOT `correlationId` at root)
- `meta.occurredAt` (NOT `timestamp`)
- `data` (NOT `payload`)

### Event Naming Convention
Format: `{domain}.{action}` (eventType in meta) and `{domain}.{action}.{version}` (topic name)
- Domain: Service boundary (booking, inventory, payment)
- Action: Past tense or requested verb (reserved, rejected, requested, confirmed)
- Version: API version in topic name (v1)

### Idempotency Requirements
- All event handlers MUST check for duplicate `meta.eventId` before processing
- Use database constraints or caching to prevent duplicate processing
- Handlers should be side-effect free when processing duplicates

## State Management Rules

### Booking Status Lifecycle
- **PENDING_PAYMENT**: Initial state after booking creation, awaiting saga completion
- **CONFIRMED**: Terminal state, inventory reserved and payment succeeded
- **REJECTED**: Terminal state, inventory unavailable or payment failed
- **CANCELLED**: Terminal state (future feature)

### State Transition Rules
- Terminal states (CONFIRMED, REJECTED, CANCELLED) are immutable
- Attempting to update terminal state should be ignored (not error)
- Only PENDING_PAYMENT bookings can transition to CONFIRMED or REJECTED

## Data Consistency Patterns

### Eventual Consistency
- Services maintain their own data stores
- Cross-service queries may show stale data briefly
- Use correlationId for debugging inconsistencies

### Compensation Logic
- Payment failure triggers inventory release via `inventory.release.requested.v1`
- Inventory service handles compensation automatically
- No manual rollback required

## API Design Conventions

### Request/Response DTOs
- Requests: `Create{Entity}Request`, `Update{Entity}Request`
- Responses: `{Entity}Response`, `{Entity}StatusResponse`
- Use Java records for immutability where possible

### Error Handling
- Return appropriate HTTP status codes (400, 404, 409, 500)
- Include meaningful error messages in response body
- Log errors with correlationId for traceability

### Validation
- Use Bean Validation annotations (@NotNull, @NotBlank, @Positive)
- Validate at API boundary before publishing events
- Return 400 Bad Request for validation failures

## Testing Considerations

### Event-Driven Testing
- **ALWAYS read actual EventPublisher and EventEnvelope classes before writing tests**
- Test event handlers independently with mock events
- Verify idempotency by processing same event twice
- Test compensation scenarios (payment failure, inventory rejection)

### Integration Testing
- Use embedded Kafka or Testcontainers for integration tests
- Verify end-to-end saga flows
- Test timeout and retry scenarios

## Correlation and Tracing

- Generate correlationId at API entry point (booking creation)
- Propagate correlationId through all events in saga (via meta.correlationId)
- Log correlationId with all significant operations
- Use correlationId to trace requests across service boundaries
