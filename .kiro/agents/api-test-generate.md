# API Test Generator

## Purpose
Generate functional API tests for a specific service.
Supports JSON REST, XML REST, and SOAP services.

## When to Use
- Adding tests for a new service
- Filling coverage gaps identified by audit
- Generating tests after API changes

## Parameters
When invoking this agent, specify:
- **SERVICE**: booking-service | inventory-service | payment-service | baggage-service | loyalty-service
- **MODE**: delta (default) | full

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/gentests.md` - Test generation rules

## Service-Specific Prompts
- Booking (JSON REST): `prompts/05-generate-booking-tests.md`
- Inventory (JSON REST): `prompts/07-generate-inventory-tests.md`
- Baggage (XML REST): `prompts/10-generate-baggage-tests.md`
- Loyalty (SOAP): `prompts/11-generate-loyalty-tests.md`

## Task

### MODE=delta (Default)
- Add missing tests ONLY
- Do NOT refactor or rewrite existing tests
- Do NOT rename test classes or methods

### MODE=full
- Refactor allowed
- Scope strictly limited to the target service
- Do NOT touch other services' tests

## Test Categories to Generate

### For JSON REST Services (booking, inventory, payment)
1. **Smoke Tests**: GET /api-docs returns 200
2. **Correlation ID Tests**: Verify X-Correlation-Id echo
3. **Happy Path Tests**: Create → Get → Update → Delete lifecycle
4. **Negative Tests**: Invalid JSON, missing fields, not found
5. **ErrorResponse Validation**: Validate error contract

### For XML REST Services (baggage)
1. **Smoke Tests**: GET /api-docs returns 200
2. **Happy Path Tests**: Checkin → Update Status → Track
3. **Negative Tests**: Invalid XML, missing elements, not found
4. **XML Validation**: Validate namespace and structure

### For SOAP Services (loyalty)
1. **WSDL Smoke Test**: GET /ws?wsdl returns 200
2. **Happy Path Tests**: Enroll → Get Status → Accrue Points
3. **SOAP Fault Tests**: Non-existent member, duplicate email
4. **Fault Validation**: Validate LoyaltyFault schema

## Output
- Output ONLY diffs for created/modified files
- Use TestNG (not JUnit)
- Use appropriate client for service type:
  - JSON REST: RestAssuredApiClient
  - XML REST: XmlApiClient
  - SOAP: SoapClient

## Example Usage

To generate tests for booking service:
```
Run api-test-generate for SERVICE=booking-service MODE=delta
```

To generate tests for baggage service (XML REST):
```
Run api-test-generate for SERVICE=baggage-service MODE=delta
```

To generate tests for loyalty service (SOAP):
```
Run api-test-generate for SERVICE=loyalty-service MODE=delta
```
