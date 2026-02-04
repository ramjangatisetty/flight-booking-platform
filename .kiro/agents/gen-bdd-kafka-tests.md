# Agent: Generate BDD Kafka Event Tests

Generate Cucumber/Gherkin feature files and step definitions for Kafka event verification testing.

## Purpose
Create BDD tests that verify Kafka events are correctly published after API operations.
Provides business-readable tests for event-driven architecture validation.

## When to Use
- When testing event publication after API operations
- When validating saga choreography flows
- When verifying event envelope structure and correlation

## Parameters
When invoking this agent, specify:
- **SERVICE**: booking-service | inventory-service | payment-service
- **MODE**: audit | delta (default) | full

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/GHERKIN_STYLE_GUIDE.md` - Gherkin writing standards
5. `prompts/13-generate-kafka-event-tests.md` - Kafka event test patterns

## MANDATORY DISCOVERY PHASE

Before generating ANY feature files, you MUST read:

1. **EventEnvelope** - `services/{service}-service/src/main/java/**/messaging/event/EventEnvelope.java`
2. **EventPublisher** - `services/{service}-service/src/main/java/**/messaging/producer/*EventPublisher.java`
3. **Event Classes** - `services/{service}-service/src/main/java/**/messaging/event/*Event.java`

## Output Locations

### Feature Files
```
api-tests/src/test/resources/features/{service}-service/events.feature
```

### Step Definitions
```
api-tests/src/test/java/framework/bdd/KafkaStepDefinitions.java
```

### Framework Components
```
api-tests/src/test/java/framework/kafka/
├── TestKafkaConsumer.java    # Kafka consumer for tests
├── ConsumedEvent.java        # Event record with parsed JSON
└── KafkaTestConfig.java      # Kafka configuration
```

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

## Service Topics

| Service | Topics |
|---------|--------|
| booking-service | `inventory.reserve.requested.v1`, `booking.confirmed.v1`, `booking.rejected.v1` |
| inventory-service | `inventory.reserved.v1`, `inventory.rejected.v1`, `inventory.released.v1` |
| payment-service | `payment.succeeded.v1`, `payment.failed.v1` |

## Feature File Structure

### Required Tags
- Service tag: `@booking`, `@inventory`, `@payment`
- Category: `@kafka`, `@events`
- Scenario ID: `@id=SERVICE-EVENT-NNN`

### Example Feature (Booking Events)
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
  Scenario: Event envelope contains required metadata
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

## Step Definition Patterns

### Kafka Subscription Steps
```java
@Given("I am subscribed to Kafka topic {string}")
@Given("I am subscribed to Kafka topics:")
```

### Event Assertion Steps
```java
@Then("I should receive a Kafka event with type {string} within {int} seconds")
@Then("the event correlationId should match the request correlationId")
@Then("the event data should contain field {string}")
@Then("the event meta should contain field {string}")
@Then("the event meta {string} should equal {string}")
@Then("the event data {string} should equal {string}")
```

## Framework Components

### TestKafkaConsumer
- Creates consumer with unique group ID per test run
- Subscribes to specified topics
- `waitForEvent(correlationId, timeout)` - Wait for event by correlation
- `waitForEventOfType(eventType, correlationId, timeout)` - Wait for specific type
- `clearBuffer()` - Clear consumed events before test

### ConsumedEvent
- `topic` - Source topic
- `eventType` - From meta.eventType
- `correlationId` - From meta.correlationId
- `getData()` - Returns data section as JsonNode
- `getMeta()` - Returns meta section as JsonNode

## Running Kafka BDD Tests

```bash
# Run all Kafka event tests
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@kafka"

# Run booking event tests
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@booking and @kafka"
```

## Prerequisites

- Kafka running on localhost:9092
- Service under test running
- Topics created (auto-created by services)

## Output
- Agent name: GenBddKafkaTests
- Mode: audit | delta | full
- Service: {service-name}
- Files created/modified
- Diffs only
