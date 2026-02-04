# Generate E2E Cross-Service Integration Tests

## Context
- Service Type: Cross-Service E2E
- Mode: delta
- Contract Source: DTOs + Event Listeners/Publishers + WSDL

## Governing Documents
Follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/12-generate-e2e-saga-tests.md` - E2E saga test generation

## Task

Generate end-to-end cross-service integration tests that verify business flows spanning multiple services.

## MANDATORY DISCOVERY PHASE (DO THIS FIRST)

Before generating ANY tests, you MUST:

### Step 1: Scan DTOs for Foreign Key References
Read ALL request/response DTOs and identify fields that reference other services:
```
services/booking-service/src/main/java/**/api/dto/*.java
services/baggage-service/src/main/java/**/api/dto/*.java
services/inventory-service/src/main/java/**/api/dto/*.java
```

Look for:
- `memberId` → Loyalty Service integration
- `bookingId` → Booking Service integration
- `bagTag` → Baggage Service integration
- `reservationId` → Inventory Service integration

**IMPORTANT**: Optional fields often represent integration points - DO NOT IGNORE THEM.

### Step 2: Read Event Listeners
For each service, read:
```
services/{service}-service/src/main/java/**/messaging/consumer/*Listener.java
```
Document what events each service consumes and what actions are triggered.

### Step 3: Read Event Publishers
For each service, read:
```
services/{service}-service/src/main/java/**/messaging/producer/*Publisher.java
```
Document what events each service publishes.

### Step 4: Map Complete Flows
Document the complete flow for each business scenario before generating tests.

## Required Test Files

Generate tests in `api-tests/src/test/java/tests/e2e/`:

### 1. LoyaltyBookingIntegrationTest.java
Tests booking + loyalty integration:
- `shouldAccrueLoyaltyPointsWhenBookingConfirmed` - Enroll member → Create booking with memberId → Verify points accrued
- `shouldNotAccruePointsWhenNoMemberId` - Create booking without memberId → Verify no accrual

### 2. BaggageBookingIntegrationTest.java
Tests booking + baggage integration:
- `shouldAutoCreateBaggageWhenBookingConfirmed` - Create booking → Wait for CONFIRMED → Verify bagTag populated
- `shouldTrackBaggageByBookingId` - Verify baggage trackable after auto-creation

### 3. FullJourneyE2ETest.java (Optional)
Complete flow: Loyalty enrollment → Booking with memberId → Inventory → Payment → Baggage

## Test Groups
Use these TestNG groups:
- `e2e` - All end-to-end tests
- `integration` - Cross-service integration tests
- `loyalty` - Loyalty-specific tests
- `baggage` - Baggage-specific tests
- `saga` - Saga flow tests

## Framework Components to Use
- `RestAssuredApiClient` - For JSON REST services
- `XmlApiClient` - For Baggage Service (XML REST)
- `SoapClientImpl` - For Loyalty Service (SOAP)
- `LoyaltySoapRequestBuilder` - For building SOAP envelopes
- `SoapResponseParser` - For parsing SOAP responses
- `CorrelationIdSupport` - For correlation ID headers
- `ReportLogger` - For ExtentReports integration

## Output
- Output diffs only
- Use TestNG (not JUnit)
- Include ReportLogger calls for all assertions
