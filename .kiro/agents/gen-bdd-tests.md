# Agent: Generate BDD/Cucumber Tests

Generate Cucumber/Gherkin feature files and step definitions for API testing.

## Purpose
Create business-readable BDD tests using Cucumber that complement the existing TestNG tests.
Provides an alternative testing format for teams preferring Gherkin syntax.

## When to Use
- When business stakeholders need readable test documentation
- When following BDD (Behavior-Driven Development) practices
- When you want living documentation of API behavior
- As an alternative to TestNG-style tests

## Parameters
When invoking this agent, specify:
- **SERVICE**: booking-service | inventory-service | payment-service | baggage-service | loyalty-service
- **MODE**: audit | delta (default) | full

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/GHERKIN_STYLE_GUIDE.md` - Gherkin writing standards
5. `prompts/gensteps.md` - Step definition generation rules

## Output Locations

### Feature Files
```
api-tests/src/test/resources/features/{service}/
├── {service}.feature           # Main feature file
├── smoke.feature               # Smoke tests
├── negative.feature            # Negative/error scenarios
└── e2e.feature                 # E2E scenarios (if applicable)
```

### Step Definitions
```
api-tests/src/test/java/tests/{service}/steps/
├── {Service}Steps.java         # Service-specific steps
└── {Service}Hooks.java         # Before/After hooks
```

## Service Type Mapping

| Service | Type | Client | Tags |
|---------|------|--------|------|
| booking-service | JSON REST | RestAssuredApiClient | `@booking` |
| inventory-service | JSON REST | RestAssuredApiClient | `@inventory` |
| payment-service | JSON REST | RestAssuredApiClient | `@payment` |
| baggage-service | XML REST | XmlApiClient | `@baggage @xmlRest` |
| loyalty-service | SOAP | SoapClientImpl | `@loyalty @soap` |

## MANDATORY DISCOVERY PHASE

Before generating ANY feature files, you MUST:

### Step 1: Read OpenAPI/WSDL Snapshot
```
api-tests/src/test/resources/openapi-snapshots/{service}/openapi.json
api-tests/src/test/resources/wsdl-snapshots/{service}/*.wsdl
```

### Step 2: Read Existing Framework Components
```
api-tests/src/test/java/framework/bdd/CommonStepDefinitions.java
api-tests/src/test/java/framework/bdd/TestContext.java
api-tests/src/test/java/framework/endpoints/{Service}Endpoints.java
```

### Step 3: Check Existing Feature Files
```
api-tests/src/test/resources/features/{service}/*.feature
```

## Feature File Structure

### Required Tags (per GHERKIN_STYLE_GUIDE.md)
- Service tag: `@booking`, `@inventory`, `@payment`, `@baggage`, `@loyalty`
- Service type tag (if non-JSON): `@xmlRest`, `@soap`
- Scenario ID: `@id=SERVICE-NNN`
- Category: `@smoke`, `@happyPath`, `@negative`, `@headers`, `@soapFault`

### Example: JSON REST (Booking)
```gherkin
@booking
Feature: Booking Service API

  @smoke @id=BOOKING-001
  Scenario: API docs should be accessible
    Given I am testing the "booking" service
    When I call "GET" "/v3/api-docs"
    Then the response status should be 200

  @happyPath @id=BOOKING-010
  Scenario: Create a booking successfully
    Given I am testing the "booking" service
    And I ensure a correlation id header is present
    And I set an idempotency key header
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    And the response json "status" should equal "PENDING_PAYMENT"
```

### Example: XML REST (Baggage)
```gherkin
@baggage @xmlRest
Feature: Baggage Service XML API

  @smoke @id=BAGGAGE-001
  Scenario: API docs should be accessible
    Given I am testing the "baggage" service
    When I call "GET" "/v3/api-docs"
    Then the response status should be 200

  @happyPath @id=BAGGAGE-010
  Scenario: Check in baggage successfully
    Given I am testing the "baggage" service
    And I have a valid BaggageCheckinRequest XML payload
    When I send XML request to "/baggage/checkin"
    Then the response status should be 200
    And the response should be valid XML
    And I capture XML element "bagTag" as "bagTag"
```

### Example: SOAP (Loyalty)
```gherkin
@loyalty @soap
Feature: Loyalty Service SOAP API

  @smoke @id=LOYALTY-001
  Scenario: WSDL should be accessible
    Given I am testing the "loyalty" service
    When I call "GET" "/ws?wsdl"
    Then the response status should be 200

  @happyPath @id=LOYALTY-010
  Scenario: Enroll a new loyalty member
    Given I am testing the "loyalty" service
    And I have a valid EnrollMemberRequest SOAP request
    And I set SOAP element "firstName" to "John"
    And I set SOAP element "lastName" to "Doe"
    And I set SOAP element "email" to "john.doe@example.com"
    When I call SOAP operation "EnrollMember"
    Then the SOAP response should be successful
    And I capture SOAP element "memberId" as "memberId"
```

## Step Definition Rules

### Reuse Common Steps
The framework provides `CommonStepDefinitions.java` with reusable steps:
- `Given I am testing the {string} service`
- `Given I set header {string} to {string}`
- `Given I ensure a correlation id header is present`
- `Given I set an idempotency key header`
- `When I call {string} {string}`
- `Then the response status should be {int}`
- `And I capture {string} as {string}`
- `And the response json {string} should equal {string}`

### Create Service-Specific Steps Only When Needed
Only create new step definitions for:
- Service-specific payload builders
- Complex assertions not covered by common steps
- SOAP-specific operations (for loyalty-service)
- XML-specific operations (for baggage-service)

## MODE Definitions

### MODE=audit
- Read-only analysis
- Report existing feature files and step coverage
- Identify missing scenarios based on OpenAPI/WSDL
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

## Running Cucumber Tests

```bash
# Run all Cucumber tests (JUnit 5 Platform)
./gradlew :api-tests:cucumberTest

# Run specific service tests by tag
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@booking"

# Run smoke tests only
./gradlew :api-tests:cucumberTest -Dcucumber.filter.tags="@smoke"

# Run TestNG smoke tests
./gradlew :api-tests:smokeTest

# Run both TestNG and Cucumber tests
./gradlew :api-tests:test
```

## Output
- Agent name: GenBddTests
- Mode: audit | delta | full
- Service: {service-name}
- Contract source: snapshot | code
- Files created/modified
- Diffs only (no extra prose)

## Example Usage

Generate BDD tests for booking service:
```
Follow .kiro/agents/gen-bdd-tests.md for SERVICE=booking-service MODE=delta
```

Generate BDD tests for loyalty service (SOAP):
```
Follow .kiro/agents/gen-bdd-tests.md for SERVICE=loyalty-service MODE=delta
```

Audit existing BDD coverage:
```
Follow .kiro/agents/gen-bdd-tests.md for SERVICE=inventory-service MODE=audit
```
