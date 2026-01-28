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
- **Publishes**: `booking.created.v1`
- **Consumes**: `inventory.reserved.v1`, `inventory.rejected.v1`

### Inventory Service (Port 8082)
- **Responsibility**: Seat availability and reservation management
- **Owns**: Inventory items and reservations (flightId, seatClass, available/reserved counts)
- **Exposes**: Admin API for inventory management, query API for availability
- **Publishes**: `inventory.reserved.v1`, `inventory.rejected.v1`
- **Consumes**: `booking.created.v1`, `payment.succeeded.v1`, `payment.failed.v1`

### Payment Service (Port 8083)
- **Responsibility**: Payment processing simulation
- **Publishes**: `payment.succeeded.v1`, `payment.failed.v1`
- **Consumes**: `inventory.reserved.v1`

## Saga Orchestration Pattern

The booking workflow implements a choreography-based saga:

```
CREATE BOOKING → RESERVE INVENTORY → PROCESS PAYMENT → CONFIRM/REJECT
     ↓                  ↓                    ↓              ↓
booking.created → inventory.reserved → payment.* → status update
                       ↓
                inventory.rejected → REJECTED
```

### Happy Path
1. User POST `/api/bookings` → Booking created with PENDING status
2. `booking.created.v1` published with correlationId
3. Inventory service reserves seat → `inventory.reserved.v1`
4. Payment service processes → `payment.succeeded.v1`
5. Booking status → CONFIRMED

### Failure Scenarios
- **Insufficient inventory**: `inventory.rejected.v1` → Booking status REJECTED
- **Payment failure**: `payment.failed.v1` → Inventory releases reservation → Booking status REJECTED

## Event Design Principles

### Event Envelope Structure
All events use `EventEnvelope<T>` wrapper:
- `eventId`: UUID for deduplication
- `eventType`: Domain event name (e.g., `booking.created.v1`)
- `timestamp`: Event creation time
- `correlationId`: Trace ID across service boundaries
- `payload`: Event-specific data

### Event Naming Convention
Format: `{domain}.{action}.{version}`
- Domain: Service boundary (booking, inventory, payment)
- Action: Past tense verb (created, reserved, rejected, succeeded, failed)
- Version: API version (v1, v2)

### Idempotency Requirements
- All event handlers MUST check for duplicate `eventId` before processing
- Use database constraints or caching to prevent duplicate processing
- Handlers should be side-effect free when processing duplicates

## State Management Rules

### Booking Status Lifecycle
- **PENDING**: Initial state, awaiting inventory reservation
- **CONFIRMED**: Terminal state, inventory reserved and payment succeeded
- **REJECTED**: Terminal state, inventory unavailable or payment failed
- **CANCELLED**: Terminal state (future feature)

### State Transition Rules
- Terminal states (CONFIRMED, REJECTED, CANCELLED) are immutable
- Attempting to update terminal state should be ignored (not error)
- Only PENDING bookings can transition to CONFIRMED or REJECTED

## Data Consistency Patterns

### Eventual Consistency
- Services maintain their own data stores
- Cross-service queries may show stale data briefly
- Use correlationId for debugging inconsistencies

### Compensation Logic
- Payment failure triggers inventory release via `payment.failed.v1`
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
- Test event handlers independently with mock events
- Verify idempotency by processing same event twice
- Test compensation scenarios (payment failure, inventory rejection)

### Integration Testing
- Use embedded Kafka or Testcontainers for integration tests
- Verify end-to-end saga flows
- Test timeout and retry scenarios

## Correlation and Tracing

- Generate correlationId at API entry point (booking creation)
- Propagate correlationId through all events in saga
- Log correlationId with all significant operations
- Use correlationId to trace requests across service boundaries
