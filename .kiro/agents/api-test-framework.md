# API Test Framework Generator

## Purpose
Create or evolve the shared API test automation framework.
This agent creates the framework skeleton with support for:
- JSON REST services (RestAssuredApiClient)
- XML REST services (XmlApiClient)
- SOAP services (SoapClient)
- BDD/Cucumber testing (TestContext, CommonStepDefinitions, CucumberTestRunner)

## When to Use
- Initial setup of the api-tests module
- Adding new framework components (clients, utilities, asserters)
- Updating framework dependencies
- Adding BDD/Cucumber infrastructure

## Parameters
When invoking this agent, specify:
- **TYPE**: testng (default) | bdd | all

### TYPE Definitions
| TYPE | Description | What Gets Created |
|------|-------------|-------------------|
| `testng` | Traditional TestNG framework only | Core framework + smoke tests (no `bdd/` package, no `features/`) |
| `bdd` | BDD/Cucumber infrastructure only | `bdd/` package + `features/` directories (assumes core framework exists) |
| `all` | Both TestNG and BDD | Everything - core framework, smoke tests, BDD infrastructure |

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/03-generate-framework-skeleton.md` - Detailed framework structure
5. `prompts/GHERKIN_STYLE_GUIDE.md` - BDD/Cucumber conventions (when TYPE=bdd or TYPE=all)

## Task

### TYPE=testng (default) - TestNG Framework Only

#### If api-tests module does NOT exist:
1. Create `api-tests/build.gradle` with dependencies:
   - RestAssured 5.5.0
   - TestNG 7.10.2
   - AssertJ 3.26.3
   - Jackson (databind, jsr310, dataformat-xml) 2.17.2
   - ExtentReports 5.1.2
   - SLF4J Simple 2.0.16
   - **Note**: Do NOT include Cucumber dependencies

2. Update `settings.gradle` to include `api-tests`

3. Create framework packages under `api-tests/src/test/java/framework/`:
   - `config/` - TestConfig, ServiceType enum
   - `clients/` - ApiClient interface, RestAssuredApiClient
   - `xml/` - XmlApiClient, XmlRequestBuilder, BaggageCheckinXmlBuilder
   - `soap/` - SoapClient, SoapClientImpl, SoapEnvelopeBuilder, SoapResponseParser
   - `endpoints/` - Endpoint constants per service
   - `requests/` - Request builders
   - `models/` - Request/response models (request/, response/, common/)
   - `mappers/` - ErrorResponseMapper, XmlResponseMapper
   - `asserters/` - ErrorAsserter, SoapFaultAsserter, XmlResponseAsserter
   - `headers/` - CorrelationIdSupport, IdempotencyKeySupport
   - `utils/` - EnvUtils, UuidUtils, JsonUtils, XmlUtils
   - `testkit/` - LocalTestClient, LocalTestGuard, FailureConfig
   - `reporting/` - ExtentReportManager, ExtentTestListener, ReportLogger
   - **Note**: Do NOT create `bdd/` package

4. Create smoke tests under `api-tests/src/test/java/tests/smoke/`

5. Create resource directories:
   - `api-tests/src/test/resources/openapi-snapshots/{service}/`
   - `api-tests/src/test/resources/wsdl-snapshots/loyalty-service/`
   - **Note**: Do NOT create `features/` directories

6. Create configuration files:
   - `api-tests/src/test/resources/testng.xml`

### TYPE=bdd - BDD/Cucumber Infrastructure Only

**Auto-bootstrap**: If core framework doesn't exist, automatically create it first.

Check if these files exist:
- `api-tests/build.gradle`
- `api-tests/src/test/java/framework/clients/ApiClient.java`
- `api-tests/src/test/java/framework/config/TestConfig.java`

If these files do NOT exist:
1. First, create the core framework (same as TYPE=testng)
2. Then, add BDD infrastructure on top

If these files exist:
1. Add Cucumber dependencies to existing `api-tests/build.gradle`:
   - `org.junit.platform:junit-platform-suite`
   - `org.junit.jupiter:junit-jupiter`
   - `io.cucumber:cucumber-java:7.18.1`
   - `io.cucumber:cucumber-junit-platform-engine:7.18.1`
   - `io.cucumber:cucumber-picocontainer:7.18.1`
   - `tech.grasshopper:extentreports-cucumber7-adapter:1.14.0`

2. Create BDD package under `api-tests/src/test/java/framework/bdd/`:
   - `TestContext.java` - Shared state for scenarios
   - `CommonStepDefinitions.java` - Reusable HTTP/header/assertion steps
   - `BookingStepDefinitions.java` - Booking payload steps
   - `SoapStepDefinitions.java` - SOAP-specific steps (loyalty)
   - `XmlStepDefinitions.java` - XML REST steps (baggage)
   - `CucumberTest.java` - JUnit 5 Platform Suite runner (industry standard)

3. Create feature file directories:
   - `api-tests/src/test/resources/features/booking-service/`
   - `api-tests/src/test/resources/features/inventory-service/`
   - `api-tests/src/test/resources/features/payment-service/`
   - `api-tests/src/test/resources/features/baggage-service/`
   - `api-tests/src/test/resources/features/loyalty-service/`

4. Create ExtentReports config for Cucumber:
   - `api-tests/src/test/resources/extent.properties`
   - `api-tests/src/test/resources/extent-config.xml`

### TYPE=all - Both TestNG and BDD

Combines TYPE=testng and TYPE=bdd:
1. Create full framework with all packages including `bdd/`
2. Include both TestNG and Cucumber dependencies
3. Create all resource directories including `features/`
4. Create all configuration files

### If api-tests module already exists:
- Modify ONLY the required framework classes based on TYPE
- Do NOT recreate the module or overwrite existing files unless necessary

## Service Types Supported
| Service | Type | Client | BDD Steps |
|---------|------|--------|-----------|
| booking-service | JSON REST | RestAssuredApiClient | CommonStepDefinitions, BookingStepDefinitions |
| inventory-service | JSON REST | RestAssuredApiClient | CommonStepDefinitions |
| payment-service | JSON REST | RestAssuredApiClient | CommonStepDefinitions |
| baggage-service | XML REST | XmlApiClient | XmlStepDefinitions |
| loyalty-service | SOAP | SoapClient | SoapStepDefinitions |

## BDD/Cucumber Infrastructure
The framework supports two testing approaches:
1. **TestNG tests** - Traditional programmatic tests in `tests/` package
2. **Cucumber BDD tests** - Feature files in `resources/features/` with step definitions in `framework/bdd/`

### BDD Package Structure
```
framework/bdd/
├── TestContext.java           # Shared state for scenarios
├── CommonStepDefinitions.java # Reusable steps (HTTP, headers, assertions)
├── BookingStepDefinitions.java # Booking-specific payload steps
├── SoapStepDefinitions.java   # SOAP-specific steps (loyalty)
├── XmlStepDefinitions.java    # XML REST steps (baggage)
└── CucumberTest.java          # JUnit 5 Platform Suite runner
```

### Feature File Structure
```
resources/features/
├── booking-service/booking.feature
├── inventory-service/inventory.feature
├── payment-service/payment.feature
├── baggage-service/baggage.feature
└── loyalty-service/loyalty.feature
```

## Output
- Output ONLY diffs for created/modified files
- Do NOT generate service-specific functional tests (only smoke tests for TYPE=testng/all)
- Do NOT use JUnit (TestNG only)
- Do NOT access domain/application packages

## Example Usage

Generate TestNG framework only (default):
```
Follow .kiro/agents/api-test-framework.md
```
or
```
Follow .kiro/agents/api-test-framework.md TYPE=testng
```

Add BDD infrastructure to existing framework:
```
Follow .kiro/agents/api-test-framework.md TYPE=bdd
```

Generate complete framework with both TestNG and BDD:
```
Follow .kiro/agents/api-test-framework.md TYPE=all
```
