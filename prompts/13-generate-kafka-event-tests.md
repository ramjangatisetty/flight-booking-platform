# Prompt 13 — Generate Kafka Event Verification Tests

Follow `TEST_GENERATION_BLUEPRINT.md`.
Follow `00-agent-operating-rules.md`.

Service Type: **Kafka Events**
Contract Source: Event envelope structure + service event publishers

## MANDATORY DISCOVERY PHASE (DO THIS FIRST!)

**Before writing ANY test code, you MUST read these files:**

1. **EventPublisher** - `services/{service}-service/src/main/java/**/messaging/producer/*EventPublisher.java`
   - Extract actual topic names (e.g., `TOPIC_INVENTORY_RESERVE_REQUESTED_V1`)
   - Extract actual event types published
   - DO NOT assume topic names like `booking.events` exist

2. **EventEnvelope** - `services/{service}-service/src/main/java/**/messaging/event/EventEnvelope.java`
   - Extract exact JSON structure
   - Note: Structure may be `{ meta: {...}, data: {...} }` NOT `{ eventId, payload }`

3. **Event Classes** - `services/{service}-service/src/main/java/**/messaging/event/*Event.java`
   - Extract payload field names

**FAILURE TO READ THESE FILES WILL RESULT IN INCORRECT TESTS.**

## Purpose

Generate black-box tests that verify Kafka events are correctly published after API operations.
This validates the event-driven architecture without accessing internal service code.

## Prerequisites

- Service under test running
- Kafka running on localhost:9092 (configurable via KAFKA_BOOTSTRAP_SERVERS)
- Topic exists and service is publishing to it

## Framework Components

### TestKafkaConsumer (`framework/kafka/TestKafkaConsumer.java`)
- Creates a test consumer with unique group ID per test run
- Subscribes to specified topic(s) - supports single topic or list of topics
- Provides `waitForEvent(correlationId, timeout)` method
- Provides `waitForEventOfType(eventType, correlationId, timeout)` method
- Returns `ConsumedEvent` record with parsed JSON
- Extracts correlationId and eventType from `meta` section

**Critical Consumer Configuration:**
- `AUTO_OFFSET_RESET_CONFIG = "earliest"` - Ensures events aren't missed (each test run uses unique group ID)
- `GROUP_ID_CONFIG = "api-test-consumer-" + UUID.randomUUID()` - Unique per test run
- Multiple initialization polls (5 x 200ms) to ensure consumer is fully joined to group
- `SESSION_TIMEOUT_MS_CONFIG = "10000"` and `HEARTBEAT_INTERVAL_MS_CONFIG = "3000"` for faster rebalancing

### ConsumedEvent Record
```java
record ConsumedEvent(
    String topic,
    String key,
    String rawValue,
    JsonNode json,
    String eventType,
    String correlationId,
    long timestamp
) {
    JsonNode getData();      // Returns the "data" section (payload)
    JsonNode getMeta();      // Returns the "meta" section
    String getEventId();     // Returns meta.eventId
    String getProducer();    // Returns meta.producer
}
```

## Event Envelope Structure (Actual from booking-service)

All events follow the `EventEnvelope<T>` structure with `meta` and `data` sections:
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
  "data": { ... payload ... }
}
```

**IMPORTANT:** The event structure uses:
- `meta.eventId` (NOT `eventId` at root)
- `meta.eventType` (NOT `eventType` at root)
- `meta.correlationId` (NOT `correlationId` at root)
- `meta.occurredAt` (NOT `timestamp`)
- `data` (NOT `payload`)

## Actual Kafka Topics (from BookingEventPublisher)

| Topic Name | Event Type | When Published |
|------------|------------|----------------|
| `inventory.reserve.requested.v1` | `inventory.reserve.requested` | When booking is created |
| `inventory.release.requested.v1` | `inventory.release.requested` | When inventory needs release |
| `payment.requested.v1` | `payment.requested` | When payment is requested |
| `booking.confirmed.v1` | `booking.confirmed` | When booking saga completes successfully |
| `booking.rejected.v1` | `booking.rejected` | When booking is rejected |

**NOTE:** There is NO `booking.events` topic or `booking.created.v1` event. When a booking is created, the service publishes `inventory.reserve.requested` to start the saga.

## Generate tests in `api-tests/src/test/java/tests/events/{Service}EventTests`:

### For Booking Service (`BookingEventTests`)

**Topics to subscribe:** `inventory.reserve.requested.v1`, `booking.confirmed.v1`, `booking.rejected.v1`

**Test: shouldPublishInventoryReserveRequestedEventAfterCreateBooking**
1. Generate unique correlationId
2. POST /bookings with correlationId header
3. Wait for event with matching correlationId on `inventory.reserve.requested.v1` topic
4. Verify eventType is `inventory.reserve.requested`
5. Verify correlationId matches request
6. Verify data contains bookingId

**Test: shouldIncludeEventEnvelopeFields**
1. Create booking
2. Consume event
3. Verify event has `meta` section with: eventId, eventType, eventVersion, occurredAt, correlationId, producer
4. Verify event has `data` section
5. Verify producer is `booking-service`

**Test: shouldHaveUniqueEventIdPerEvent**
1. Create two bookings
2. Consume both events
3. Verify meta.eventId values are different

**Test: shouldPublishBookingConfirmedEventAfterSagaCompletion**
1. Seed inventory for a flight
2. Create booking for that flight
3. Wait for `booking.confirmed` event (longer timeout for saga)
4. Verify data contains bookingId and status=CONFIRMED

### For Inventory Service (`InventoryEventTests`)

**Topics:** `inventory.reserved.v1`, `inventory.rejected.v1`, `inventory.released.v1`

**Test: shouldPublishInventoryReservedEvent**
1. Seed inventory
2. Create booking (triggers reservation via saga)
3. Wait for `inventory.reserved` event
4. Verify data contains reservationId, bookingId

**Test: shouldPublishInventoryRejectedEvent**
1. Create booking for non-existent flight (no inventory)
2. Wait for `inventory.rejected` event
3. Verify data contains reason

## Event Types Reference (Actual)

| Service | Topic | Event Type (meta.eventType) |
|---------|-------|-------------|
| Booking | inventory.reserve.requested.v1 | inventory.reserve.requested |
| Booking | booking.confirmed.v1 | booking.confirmed |
| Booking | booking.rejected.v1 | booking.rejected |
| Inventory | inventory.reserved.v1 | inventory.reserved |
| Inventory | inventory.rejected.v1 | inventory.rejected |
| Inventory | inventory.released.v1 | inventory.released |
| Payment | payment.succeeded.v1 | payment.succeeded |
| Payment | payment.failed.v1 | payment.failed |

## Test Groups
- `events`: All event verification tests
- `kafka`: Kafka-specific tests
- `{service}`: Service-specific tests (booking, inventory, payment)
- `e2e`: End-to-end saga tests

## Constraints
- Use TestKafkaConsumer for event consumption (supports multiple topics)
- Use CorrelationIdSupport for tracing
- Use ReportLogger for ExtentReports integration
- Default timeout: 10 seconds for event consumption
- Saga completion timeout: 30 seconds (longer for full saga flow)
- Consumer group ID must be unique per test run
- Access event data via `getData()` method, not `getPayload()`
- Access event metadata via `getMeta()` method
- Use `AUTO_OFFSET_RESET_CONFIG = "earliest"` to avoid missing events
- Clear buffer before tests that need fresh events: `kafkaConsumer.clearBuffer()`
- For saga completion tests, verify intermediate events first (e.g., `inventory.reserve.requested`) before waiting for final events
- Output diffs only

## BDD Alternative

For Cucumber/Gherkin-style Kafka event tests, see `14-generate-bdd-kafka-tests.md`.
BDD tests provide business-readable scenarios using the same underlying framework components.
