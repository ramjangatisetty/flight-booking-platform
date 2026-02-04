# Prompt 14 — Generate BDD Kafka Event Tests

Follow `TEST_GENERATION_BLUEPRINT.md`.
Follow `00-agent-operating-rules.md`.
Follow `GHERKIN_STYLE_GUIDE.md`.

Service Type: **Kafka Events (BDD)**
Contract Source: Event envelope structure + service event publishers

## Purpose

Generate Cucumber/Gherkin feature files and step definitions for Kafka event verification testing.
This provides business-readable BDD tests that validate events are correctly published after API operations.

## Parameters
- **SERVICE**: booking-service | inventory-service | payment-service
- **MODE**: audit | delta (default) | full

## MANDATORY DISCOVERY PHASE (DO THIS FIRST!)

**Before writing ANY feature files, you MUST read these files:**

1. **EventEnvelope** - `services/{service}-service/src/main/java/**/messaging/event/EventEnvelope.java`
   - Extract exact JSON structure (meta + data sections)

2. **EventPublisher** - `services/{service}-service/src/main/java/**/messaging/producer/*EventPublisher.java`
   - Extract actual topic names
   - Extract actual event types published

3. **Event Classes** - `services/{service}-service/src/main/java/**/messaging/event/*Event.java`
   - Extract payload field names

4. **Existing Framework** - `api-tests/src/test/java/framework/bdd/`
   - CommonStepDefinitions.java - reusable HTTP steps
   - KafkaStepDefinitions.java - Kafka-specific steps
   - TestContext.java - shared state

**FAILURE TO READ THESE FILES WILL RESULT IN INCORRECT TESTS.**

## Event Envelope Structure

All events use `EventEnvelope<T>` with nested structure:
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

## Service Topics Reference

| Service | Topics |
|---------|--------|
| booking-service | `inventory.reserve.requested.v1`, `booking.confirmed.v1`, `booking.rejected.v1` |
| inventory-service | `inventory.reserved.v1`, `inventory.rejected.v1`, `inventory.released.v1` |
| payment-service | `payment.succeeded.v1`, `payment.failed.v1` |

## Output Locations

### Feature Files
```
api-tests/src/test/resources/features/{service}-service/events.feature
```

### Step Definitions (if new steps needed)
```
api-tests/src/test/java/framework/bdd/KafkaStepDefinitions.java
```

## Feature File Structure

### Required Tags
- Service tag: `@booking`, `@inventory`, `@payment`
- Category tags: `@kafka`, `@events`
- Scenario ID: `@id=SERVICE-EVENT-NNN`
- E2E tag for saga tests: `@e2e`

### Available Kafka Steps (from KafkaStepDefinitions.java)

```gherkin
# Subscription
Given I am subscribed to Kafka topic {string}
Given I am subscribed to Kafka topics:
  | topic1 |
  | topic2 |
Given I clear the Kafka event buffer

# Event Assertions
Then I should receive a Kafka event with type {string} within {int} seconds
Then I should receive a Kafka event within {int} seconds
Then the event correlationId should match the request correlationId
Then the event data should contain field {string}
Then the event meta should contain field {string}
Then the event meta {string} should equal {string}
Then the event data {string} should equal {string}
Then the event should be on topic {string}

# Capture
And I capture event data {string} as {string}
And I capture event meta {string} as {string}
```

### Example Feature File

```gherkin
@booking @kafka @events
Feature: Booking Service Kafka Events

  Background:
    Given I am testing the "booking" service
    And I am subscribed to Kafka topics:
      | inventory.reserve.requested.v1 |
      | booking.confirmed.v1           |
      | booking.rejected.v1            |

  @id=BOOKING-EVENT-001
  Scenario: Inventory reserve requested event published after booking creation
    Given I ensure a correlation id header is present
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.reserve.requested" within 10 seconds
    And the event correlationId should match the request correlationId
    And the event data should contain field "bookingId"

  @id=BOOKING-EVENT-002
  Scenario: Event envelope contains required metadata fields
    Given I ensure a correlation id header is present
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.reserve.requested" within 10 seconds
    And the event meta should contain field "eventId"
    And the event meta should contain field "eventType"
    And the event meta should contain field "occurredAt"
    And the event meta should contain field "producer"
    And the event meta "producer" should equal "booking-service"
```

## Framework Components

### TestKafkaConsumer (`framework/kafka/TestKafkaConsumer.java`)
- Creates consumer with unique group ID per test run
- Subscribes to specified topics
- `waitForEvent(correlationId, timeout)` - Wait for event by correlation
- `waitForEventOfType(eventType, correlationId, timeout)` - Wait for specific type
- `clearBuffer()` - Clear consumed events before test
- `getAllEvents()` - Get all consumed events
- `getEventsByCorrelationId(correlationId)` - Filter by correlation

### ConsumedEvent (`framework/kafka/ConsumedEvent.java`)
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
    JsonNode getData();      // Returns data section
    JsonNode getMeta();      // Returns meta section
    String getEventId();     // Returns meta.eventId
    String getProducer();    // Returns meta.producer
}
```

### KafkaTestConfig (`framework/kafka/KafkaTestConfig.java`)
- `getBootstrapServers()` - Returns KAFKA_BOOTSTRAP_SERVERS or localhost:9092
- `getDefaultTimeoutSeconds()` - Returns KAFKA_TIMEOUT_SECONDS or 10
- `getSagaTimeoutSeconds()` - Returns KAFKA_SAGA_TIMEOUT_SECONDS or 30

## MODE Definitions

### MODE=audit
- Read-only analysis
- Report existing feature files and step coverage
- Identify missing scenarios based on event publishers
- NO file changes

### MODE=delta (default)
- Add missing feature files and scenarios
- Add missing step definitions
- Do NOT modify existing scenarios
- Do NOT refactor existing step definitions

### MODE=full
- Refactoring allowed
- May reorganize feature files
- May consolidate step definitions
- Scope strictly limited to target service

## Running Kafka BDD Tests

```bash
# Run all Kafka event tests
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@kafka"

# Run booking event tests
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@booking and @kafka"

# Run E2E saga tests
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@e2e and @kafka"
```

## Prerequisites

- Kafka running on localhost:9092 (or KAFKA_BOOTSTRAP_SERVERS)
- Service under test running
- Topics created (auto-created by services)

## Constraints

- Use existing KafkaStepDefinitions - only add new steps if absolutely necessary
- Use existing CommonStepDefinitions for HTTP operations
- Reuse existing payload step definitions (BookingStepDefinitions, InventoryStepDefinitions)
- Default timeout: 10 seconds for event consumption
- Saga completion timeout: 30 seconds (use @e2e tag for these)
- Output diffs only

## Output Format

```markdown
# GenBddKafkaTests Output

Agent: GenBddKafkaTests
Mode: delta
Service: booking-service
Contract source: EventEnvelope + BookingEventPublisher

## Files Created
- features/booking-service/events.feature

## Files Modified
- (none)

## Diffs
[unified diff output]
```
