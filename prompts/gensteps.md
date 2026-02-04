# GenSteps — Step Definition Generator for Cucumber/Gherkin

Generate step definitions and glue code from Gherkin feature files for one service.

## Parameters
- **SERVICE**: booking-service | inventory-service | payment-service | baggage-service | loyalty-service
- **MODE**: audit | delta (default) | full
- **ALLOW_NETWORK**: false (default) | true

---

## NON-NEGOTIABLE RULES

1. Read and follow `00-agent-operating-rules.md` strictly
2. Read and follow `TEST_GENERATION_BLUEPRINT.md` strictly
3. Read and follow `GHERKIN_STYLE_GUIDE.md` strictly
4. Read and follow `00-security-guardrails.md` strictly
5. Scope strictly to the service: $SERVICE
6. Do NOT touch tests for other services
7. Do NOT modify production code
8. Reuse existing framework components (CommonStepDefinitions, TestContext)

---

## ROLE CLARIFICATION

GenSteps translates **behavioral intent** (Gherkin) into executable step definitions.

- Gherkin defines *what* the system should do
- OpenAPI/WSDL defines *how* the API behaves
- GenSteps binds the two using the shared test framework

GenSteps MUST NOT:
- Invent new scenarios
- Change feature file intent
- Perform business logic validation beyond API behavior

---

## WRITABLE PATHS (STRICT)

In MODE=delta/full, you may create/modify ONLY:
- `api-tests/src/test/resources/features/$SERVICE/**`
- `api-tests/src/test/java/tests/$SERVICE/steps/**`

You MUST NOT modify:
- `api-tests/src/test/java/framework/**` (GenFramework only)
- `api-tests/src/test/resources/openapi-snapshots/**` (SnapshotRefresh only)
- `api-tests/src/test/resources/wsdl-snapshots/**` (SnapshotRefresh only)

---

## MODE DEFINITIONS

### MODE=audit
- Read-only
- Verify feature files exist for $SERVICE
- Verify each step has a matching step definition
- Verify scenarios satisfy data lifecycle rules (create/capture/cleanup)
- Output structured report with missing bindings and gaps
- NO diffs, NO code changes

### MODE=delta (default)
- Add missing step definitions only
- Do NOT rewrite existing scenarios
- Do NOT refactor existing step defs beyond what is required to bind missing steps

### MODE=full
- You MAY refactor step defs and glue code for consistency
- Only within $SERVICE scope

---

## DISCOVERY PRIORITY (MANDATORY)

### 1) Feature Files
```
api-tests/src/test/resources/features/$SERVICE/**/*.feature
```

### 2) Contract Source
a) OpenAPI/WSDL snapshot (preferred):
```
api-tests/src/test/resources/openapi-snapshots/$SERVICE/openapi.json
api-tests/src/test/resources/wsdl-snapshots/$SERVICE/*.wsdl
```

b) Runtime `/api-docs` or `/ws?wsdl` only if ALLOW_NETWORK=true

c) Controller + DTO code fallback

You MUST state in your output:
```
Contract source used: snapshot | runtime | code
```

---

## SERVICE TYPE HANDLING

### JSON REST Services (booking, inventory, payment)
- Use `RestAssuredApiClient` from framework
- Use `CommonStepDefinitions` for standard HTTP operations
- Create service-specific steps only for payload builders

### XML REST Services (baggage)
- Use `XmlApiClient` from framework
- Create XML-specific step definitions
- Validate XML namespace: `http://letzautomate.com/baggage/v1`
- Validate bagTag pattern: `[A-Z]{2}[0-9]{8}`

### SOAP Services (loyalty)
- Use `SoapClientImpl` from framework
- Create SOAP-specific step definitions
- Use `LoyaltySoapRequestBuilder` for envelope construction
- Handle SOAP faults with `SoapFaultAsserter`

---

## STEP DEFINITION PATTERNS

### Reuse CommonStepDefinitions
The framework provides these reusable steps in `framework.bdd.CommonStepDefinitions`:

```java
@Given("I am testing the {string} service")
@Given("I set header {string} to {string}")
@Given("I ensure a correlation id header is present")
@Given("I set an idempotency key header")
@When("I call {string} {string}")
@Then("the response status should be {int}")
@And("I capture {string} as {string}")
@And("the response json {string} should equal {string}")
```

### Service-Specific Steps Pattern

```java
package tests.booking.steps;

import framework.bdd.TestContext;
import framework.models.request.CreateBookingRequest;
import io.cucumber.java.en.Given;

public class BookingSteps {
    private final TestContext context;

    public BookingSteps(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid CreateBookingRequest JSON payload")
    public void iHaveAValidCreateBookingRequestPayload() {
        CreateBookingRequest request = new CreateBookingRequest(
            "FL" + System.currentTimeMillis(),
            "ECONOMY",
            new java.math.BigDecimal("299.99"),
            "USD",
            null  // memberId optional
        );
        context.setLastRequestBody(request);
    }
}
```

### SOAP Steps Pattern (Loyalty)

```java
package tests.loyalty.steps;

import framework.bdd.TestContext;
import framework.soap.LoyaltySoapRequestBuilder;
import framework.soap.SoapResponse;
import framework.endpoints.LoyaltyEndpoints;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import java.util.HashMap;
import java.util.Map;

public class LoyaltySteps {
    private final TestContext context;
    private final Map<String, String> soapElements = new HashMap<>();

    public LoyaltySteps(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid EnrollMemberRequest SOAP request")
    public void iHaveAValidEnrollMemberRequest() {
        soapElements.clear();
        // Use unique email to avoid "already exists" errors on repeated runs
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";
        soapElements.put("firstName", "John");
        soapElements.put("lastName", "Doe");
        soapElements.put("email", uniqueEmail);
    }

    @When("I call SOAP operation {string}")
    public void iCallSoapOperation(String operation) {
        String envelope = buildSoapEnvelope(operation);
        String soapAction = LoyaltyEndpoints.getSoapAction(operation);
        SoapResponse response = context.getSoapClient()
            .sendRequest(soapAction, envelope);
        context.setLastSoapResponse(response);
    }

    @Then("the SOAP response should be successful")
    public void theSoapResponseShouldBeSuccessful() {
        assertThat(context.getLastSoapResponse().getStatusCode())
            .isEqualTo(200);
        assertThat(context.getLastSoapResponse().isFault())
            .isFalse();
    }

    private String buildSoapEnvelope(String operation) {
        return switch (operation) {
            case "EnrollMember" -> LoyaltySoapRequestBuilder.enrollMember(
                soapElements.getOrDefault("firstName", "John"),
                soapElements.getOrDefault("lastName", "Doe"),
                soapElements.get("email")
            );
            // ... other operations
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }
}
```

### XML Steps Pattern (Baggage)

```java
package tests.baggage.steps;

import framework.bdd.TestContext;
import framework.xml.BaggageCheckinXmlBuilder;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class BaggageSteps {
    private final TestContext context;

    public BaggageSteps(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid BaggageCheckinRequest XML payload")
    public void iHaveAValidBaggageCheckinRequest() {
        // BaggageCheckinRequest requires: bookingId, passengerId, bagTag, origin, destination
        String xml = new BaggageCheckinXmlBuilder()
            .withBookingId(java.util.UUID.randomUUID().toString())
            .withPassengerId(java.util.UUID.randomUUID().toString())
            .withBagTag("AB" + String.format("%08d", System.currentTimeMillis() % 100000000))
            .withOrigin("JFK")
            .withDestination("LAX")
            .build();
        context.setLastRequestBody(xml);
    }

    @When("I send XML request to {string}")
    public void iSendXmlRequestTo(String path) {
        // Switch to XML client for XML requests
        context.setXmlClient(framework.config.ServiceType.BAGGAGE);
        var response = context.getClient().post(path, context.getHeaders(), 
            context.getLastRequestBody());
        context.setLastResponse(response);
    }
}
```

---

## DATA LIFECYCLE RULES (CRITICAL)

If a scenario requires an existing resource ID, the scenario MUST:
1. Create the resource first (POST/SOAP operation), capture the ID
2. Reuse that ID in subsequent steps (GET/PUT/PATCH/DELETE/SOAP)
3. Cleanup (DELETE/reset) where safe

**Avoid hard-coded IDs** unless using local fixtures with `/test/reset`.

---

## OUTPUT FORMAT

### MODE=audit Output
```markdown
# BDD Audit Report: $SERVICE

## Feature Files Found
- features/$SERVICE/smoke.feature (3 scenarios)
- features/$SERVICE/booking.feature (5 scenarios)

## Step Coverage
| Step | Bound | Definition Location |
|------|-------|---------------------|
| Given I am testing the "booking" service | ✅ | CommonStepDefinitions |
| Given I have a valid CreateBookingRequest | ❌ | MISSING |

## Missing Step Definitions
1. `Given I have a valid CreateBookingRequest JSON payload`
2. `Then the booking status should be {string}`

## Data Lifecycle Issues
- Scenario BOOKING-015: Uses hardcoded bookingId without create step
```

### MODE=delta/full Output
```markdown
# GenSteps Output

Agent: GenSteps
Mode: delta
Service: booking-service
Contract source: snapshot

## Files Created
- tests/booking/steps/BookingSteps.java

## Files Modified
- (none)

## Diffs
[unified diff output]
```

---

## CONSTRAINTS

- Do NOT run builds/tests/gradle/curl/network calls unless ALLOW_NETWORK=true
- JUnit 5 Platform for Cucumber (industry standard)
- TestNG for smoke tests only
- Use cucumber-picocontainer for dependency injection
- Reuse existing ExtentReports integration; do not create new reporting
- Output diffs only (no explanations unless explicitly asked)
