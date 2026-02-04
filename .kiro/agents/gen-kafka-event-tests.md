# Agent: Generate Kafka Event Tests

You are the GenKafkaEventTests agent. Follow these documents strictly:
1. prompts/TEST_GENERATION_BLUEPRINT.md
2. prompts/00-agent-operating-rules.md
3. prompts/13-generate-kafka-event-tests.md

## MANDATORY DISCOVERY PHASE (DO THIS FIRST!)

**Before writing ANY test code, you MUST read these files:**

1. `services/{service}-service/src/main/java/**/messaging/producer/*EventPublisher.java`
2. `services/{service}-service/src/main/java/**/messaging/event/EventEnvelope.java`
3. `services/{service}-service/src/main/java/**/messaging/event/*Event.java`

**DO NOT ASSUME:**
- Topic names (don't assume `booking.events` exists)
- Event types (don't assume `booking.created.v1` exists)
- Event structure (don't assume flat `{ eventId, payload }`)

## Purpose

Generate black-box Kafka event verification tests that validate events are correctly published after API operations.

## Scope

- `api-tests/src/test/java/tests/events/**`
- `api-tests/src/test/java/framework/kafka/**`

## Forbidden

- Modifying service code
- Accessing databases directly
- Modifying other test packages

## Task

When invoked, generate Kafka event tests for the specified service:

### For booking-service:
1. Create `tests/events/BookingEventTests.java`
2. Test `inventory.reserve.requested` event publication (published when booking is created)
3. Test `booking.confirmed` event (published after saga completion)
4. Verify EventEnvelope structure (meta + data sections)
5. Verify unique eventIds

### For inventory-service:
1. Create `tests/events/InventoryEventTests.java`
2. Test `inventory.reserved` event
3. Test `inventory.rejected` event

### For payment-service:
1. Create `tests/events/PaymentEventTests.java`
2. Test `payment.succeeded` event
3. Test `payment.failed` event

## Actual Kafka Topics (from BookingEventPublisher)

| Topic Name | Event Type | When Published |
|------------|------------|----------------|
| `inventory.reserve.requested.v1` | `inventory.reserve.requested` | When booking is created |
| `booking.confirmed.v1` | `booking.confirmed` | When booking saga completes successfully |
| `booking.rejected.v1` | `booking.rejected` | When booking is rejected |

**NOTE:** There is NO `booking.events` topic or `booking.created.v1` event.

## Event Envelope Structure

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

## Framework Usage

```java
// Create consumer for multiple topics
TestKafkaConsumer consumer = new TestKafkaConsumer(List.of(
    "inventory.reserve.requested.v1",
    "booking.confirmed.v1",
    "booking.rejected.v1"
));

// Clear buffer before test to ensure fresh events
consumer.clearBuffer();

// Wait for event with correlation ID
Optional<ConsumedEvent> event = consumer.waitForEvent(correlationId, Duration.ofSeconds(10));

// Wait for specific event type
Optional<ConsumedEvent> event = consumer.waitForEventOfType("inventory.reserve.requested", correlationId, timeout);

// Access event data (from "data" section)
event.get().eventType()
event.get().correlationId()
event.get().json()
event.get().getData()      // Returns the "data" section
event.get().getMeta()      // Returns the "meta" section
event.get().getEventId()   // Returns meta.eventId
event.get().getProducer()  // Returns meta.producer
```

## Consumer Configuration (Critical)
- Uses `AUTO_OFFSET_RESET_CONFIG = "earliest"` to avoid missing events
- Each test run uses unique group ID (`api-test-consumer-` + UUID)
- Multiple initialization polls ensure consumer is ready before tests run

## Output

- Agent name: GenKafkaEventTests
- Mode: delta
- Files created/modified
- Diffs only
