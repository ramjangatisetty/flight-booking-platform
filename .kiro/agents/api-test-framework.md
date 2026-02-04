# API Test Framework Generator

## Purpose
Create or evolve the shared API test automation framework.
This agent creates the framework skeleton with support for JSON REST, XML REST, and SOAP services.

## When to Use
- Initial setup of the api-tests module
- Adding new framework components (clients, utilities, asserters)
- Updating framework dependencies

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/03-generate-framework-skeleton.md` - Detailed framework structure

## Task

### If api-tests module does NOT exist:
1. Create `api-tests/build.gradle.kts` with dependencies:
   - RestAssured 5.5.0
   - TestNG 7.10.2
   - AssertJ 3.26.3
   - Jackson (databind, jsr310, dataformat-xml) 2.17.2
   - SLF4J Simple 2.0.16

2. Update `settings.gradle` to include `api-tests`

3. Create framework packages under `api-tests/src/test/java/framework/`:
   - `config/` - TestConfig for environment variables
   - `clients/` - ApiClient interface, RestAssuredApiClient
   - `xml/` - XmlApiClient for XML REST services
   - `soap/` - SoapClient for SOAP services
   - `endpoints/` - Endpoint constants per service
   - `requests/` - Request builders
   - `models/` - Request/response models
   - `mappers/` - Response mappers
   - `asserters/` - ErrorAsserter, SoapFaultAsserter, XmlResponseAsserter
   - `headers/` - CorrelationIdSupport, IdempotencyKeySupport
   - `utils/` - EnvUtils, UuidUtils, JsonUtils, XmlUtils
   - `testkit/` - LocalTestClient, LocalTestGuard

4. Create smoke tests under `api-tests/src/test/java/tests/smoke/`

5. Create snapshot directories:
   - `api-tests/src/test/resources/openapi-snapshots/`
   - `api-tests/src/test/resources/wsdl-snapshots/`

### If api-tests module already exists:
- Modify ONLY the required framework classes
- Do NOT recreate the module or overwrite existing files unless necessary

## Service Types Supported
| Service | Type | Client |
|---------|------|--------|
| booking-service | JSON REST | RestAssuredApiClient |
| inventory-service | JSON REST | RestAssuredApiClient |
| payment-service | JSON REST | RestAssuredApiClient |
| baggage-service | XML REST | XmlApiClient |
| loyalty-service | SOAP | SoapClient |

## Output
- Output ONLY diffs for created/modified files
- Do NOT generate service-specific functional tests
- Do NOT use JUnit (TestNG only)
- Do NOT access domain/application packages
