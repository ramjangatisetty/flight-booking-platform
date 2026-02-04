# Gherkin Style Guide for API Test Generation (Enterprise)

**Applies to:** Microservices functional API automation generated/maintained by agents  
**Primary goals:** traceability, readability, stable automation, controlled change  
**Last updated:** 2024-01-06

---

## 1) Why Gherkin in this repo

Gherkin is the **behavior source of truth**:
- Business-readable scenarios (for managers, BAs, QA)
- Deterministic inputs for agents to generate step definitions and API tests
- Traceability from **story → feature → scenario → automation**

Contract sources of truth:
- **OpenAPI** for JSON REST and XML REST services
- **WSDL/XSD** for SOAP services

**Rule:** Agents must not invent business behavior. If behavior is not in Gherkin (or an approved overlay), agents should **audit and report gaps**.

---

## 2) Folder structure (recommended)

Place feature files under the API test module:

```
api-tests/src/test/resources/features/
  booking-service/      (JSON REST)
  inventory-service/    (JSON REST)
  payment-service/      (JSON REST)
  baggage-service/      (XML REST)
  loyalty-service/      (SOAP)
```

Step definitions live under:

```
api-tests/src/test/java/tests/<service>/steps/
```

Framework utilities live under:

```
api-tests/src/test/java/framework/**
```

---

## 3) Mandatory tagging conventions

### A) Service tag (required)
Use a single service tag for each feature/scenario:

- `@booking` (booking-service, JSON REST)
- `@inventory` (inventory-service, JSON REST)
- `@payment` (payment-service, JSON REST)
- `@baggage` (baggage-service, XML REST)
- `@loyalty` (loyalty-service, SOAP)

### B) Service type tag (required for non-JSON)
- `@xmlRest` (for baggage-service)
- `@soap` (for loyalty-service)

### C) Scenario ID (required)
Each scenario MUST have a stable ID tag:

- `@id=BOOKING-001`
- `@id=BAGGAGE-014`
- `@id=LOYALTY-005`

This enables:
- reporting
- Jira/XRay mapping
- delta regeneration without churn

### D) Category tags (recommended)
Pick one or more:

- `@smoke`
- `@contract`
- `@happyPath`
- `@negative`
- `@headers`
- `@localOnly`
- `@soapFault` (for SOAP fault scenarios)

### E) Mode tags (optional)
If you want to distinguish generated vs hand-written:

- `@generated`
- `@handcrafted`

---

## 4) Step writing rules (keep steps reusable)

### Do
- Write steps that map to reusable automation primitives:
  - HTTP call (JSON/XML)
  - SOAP operation call
  - payload builder
  - capture value
  - assert status
  - validate schema/error response/SOAP fault
- Prefer **parameterized steps** over near-duplicates.

### Don't
- Bake service-specific logic into generic steps.
- Create one-off steps for every scenario if a parameter would do.

---

## 5) Canonical step vocabulary

### HTTP call (JSON REST)
```gherkin
When I call "<METHOD>" "<PATH>"
When I call "<METHOD>" "<PATH>" with JSON body
```

### HTTP call (XML REST)
```gherkin
When I call "<METHOD>" "<PATH>" with XML body
When I send XML request to "<PATH>"
```

### SOAP call
```gherkin
When I call SOAP operation "<OPERATION>"
When I send SOAP request "<OPERATION>" to "<ENDPOINT>"
```

### Headers
```gherkin
Given I set header "<NAME>" to "<VALUE>"
Given I ensure a correlation id header is present
Given I set an idempotency key header
Given I set SOAPAction header to "<ACTION>"
```

### Request body (JSON)
```gherkin
Given I have a valid <RequestType> JSON payload
Given I set JSON field "<jsonPath>" to "<value>"
```

### Request body (XML)
```gherkin
Given I have a valid <RequestType> XML payload
Given I set XML element "<elementName>" to "<value>"
Given I use XML namespace "<namespace>"
```

### Request body (SOAP)
```gherkin
Given I have a valid <RequestType> SOAP request
Given I set SOAP element "<elementName>" to "<value>"
```

### Assertions (JSON REST)
```gherkin
Then the response status should be <STATUS>
And the response should match JSON schema "<SchemaName>"
And the error response contract should be valid
And the response json "<jsonPath>" should equal "<value>"
```

### Assertions (XML REST)
```gherkin
Then the response status should be <STATUS>
And the response should be valid XML
And the XML element "<elementName>" should equal "<value>"
And the XML error response should be valid
```

### Assertions (SOAP)
```gherkin
Then the SOAP response should be successful
And the SOAP response element "<elementName>" should equal "<value>"
Then the SOAP response should be a fault
And the SOAP fault code should be "<faultCode>"
And the SOAP fault message should contain "<text>"
```

### Captures
```gherkin
And I capture "<jsonPath>" as "<var>"
And I capture XML element "<elementName>" as "<var>"
And I capture SOAP element "<elementName>" as "<var>"
```

### Path params
Use `{var}` placeholders in the PATH:
```gherkin
When I call "GET" "/bookings/{bookingId}"
When I call "GET" "/baggage/track/{bagTag}"
```

---

## 6) Data lifecycle rules (critical)

If a scenario requires an existing resource ID, the scenario MUST:
- create the resource first (POST/SOAP operation), capture the ID
- reuse that ID in subsequent steps (GET/PUT/PATCH/DELETE/SOAP)
- cleanup (DELETE/reset) where safe

**Avoid hard-coded IDs** unless using local fixtures with `/test/reset`.

---

## 7) Correlation ID rule

If a scenario sets/ensures `X-Correlation-Id`, it MUST validate echo:
- response header OR
- `ErrorResponse.correlationId` for negative cases
- SOAP response correlationId element (if applicable)

---

## 8) Booking Idempotency-Key rule

For booking create:
- POST `/bookings` requires `Idempotency-Key`

Scenarios that test booking create MUST:
- include `Given I set an idempotency key header`
- include a negative scenario for missing key

---

## 9) XML REST rules (Baggage Service)

For baggage-service scenarios:
- Tag with `@baggage @xmlRest`
- Use XML namespace: `http://letzautomate.com/baggage/v1`
- Validate bagTag pattern: `[A-Z]{2}[0-9]{8}`
- Use XML-specific steps for payload and assertions

Example:
```gherkin
@baggage @xmlRest @happyPath @id=BAGGAGE-010
Scenario: Check in baggage successfully
  Given I have a valid BaggageCheckinRequest XML payload
  And I use XML namespace "http://letzautomate.com/baggage/v1"
  And I set XML element "bagTag" to "AB12345678"
  When I call "POST" "/baggage/checkin" with XML body
  Then the response status should be 200
  And the response should be valid XML
  And the XML element "status" should equal "ACCEPTED"
```

---

## 10) SOAP rules (Loyalty Service)

For loyalty-service SOAP scenarios:
- Tag with `@loyalty @soap`
- Use SOAP namespace: `http://letzautomate.com/loyalty/v1`
- Include SOAPAction header
- Validate SOAP faults for negative scenarios

Example:
```gherkin
@loyalty @soap @happyPath @id=LOYALTY-010
Scenario: Enroll a new loyalty member
  Given I have a valid EnrollMemberRequest SOAP request
  And I set SOAP element "firstName" to "John"
  And I set SOAP element "lastName" to "Doe"
  And I set SOAP element "email" to "john.doe@example.com"
  When I call SOAP operation "EnrollMember"
  Then the SOAP response should be successful
  And I capture SOAP element "memberId" as "memberId"
  And the SOAP response element "tier" should equal "BASIC"
  And the SOAP response element "status" should equal "ACTIVE"

@loyalty @soap @negative @soapFault @id=LOYALTY-020
Scenario: Get status for non-existent member returns SOAP fault
  Given I have a valid GetMemberStatusRequest SOAP request
  And I set SOAP element "memberId" to "00000000-0000-0000-0000-000000000000"
  When I call SOAP operation "GetMemberStatus"
  Then the SOAP response should be a fault
  And the SOAP fault message should contain "Member not found"
```

---

## 11) Local-only testkit rule

Any `/test/*` endpoints are **local profile only**.
Mark those scenarios with:
- `@localOnly`

Agents must gate those by ENV (local).

---

## 12) What agents are allowed to do with features

### Allowed
- Generate missing step definitions and automation code to satisfy existing scenarios
- In delta mode: add missing scenarios ONLY if asked explicitly (otherwise audit)
- Fix step bindings without changing business meaning

### Not allowed (without explicit instruction)
- Rewrite scenario wording
- Change acceptance criteria
- Remove scenarios

---

## 13) Example (JSON REST - good)

```gherkin
@booking
Feature: Booking Service - Core behavior

  @smoke @id=BOOKING-001
  Scenario: api-docs should return 200
    When I call "GET" "/api-docs"
    Then the response status should be 200

  @happyPath @id=BOOKING-010
  Scenario: Create and retrieve a booking
    Given I have a valid CreateBookingRequest JSON payload
    And I set an idempotency key header
    And I ensure a correlation id header is present
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    When I call "GET" "/bookings/{bookingId}"
    Then the response status should be 200
    And the response json "bookingId" should equal "{bookingId}"
```

---

## 14) Example (XML REST - good)

```gherkin
@baggage @xmlRest
Feature: Baggage Service - XML REST behavior

  @smoke @id=BAGGAGE-001
  Scenario: api-docs should return 200
    When I call "GET" "/api-docs"
    Then the response status should be 200

  @happyPath @id=BAGGAGE-010
  Scenario: Check in and track baggage
    Given I have a valid BaggageCheckinRequest XML payload
    And I use XML namespace "http://letzautomate.com/baggage/v1"
    When I call "POST" "/baggage/checkin" with XML body
    Then the response status should be 200
    And I capture XML element "bagTag" as "bagTag"
    When I call "GET" "/baggage/track/{bagTag}"
    Then the response status should be 200
    And the response should be valid XML
```

---

## 15) Example (SOAP - good)

```gherkin
@loyalty @soap
Feature: Loyalty Service - SOAP behavior

  @smoke @id=LOYALTY-001
  Scenario: WSDL should be accessible
    When I call "GET" "/ws?wsdl"
    Then the response status should be 200
    And the response should contain "wsdl:definitions"

  @happyPath @id=LOYALTY-010
  Scenario: Enroll member and check status
    Given I have a valid EnrollMemberRequest SOAP request
    And I set SOAP element "firstName" to "Jane"
    And I set SOAP element "lastName" to "Smith"
    And I set SOAP element "email" to "jane.smith@example.com"
    When I call SOAP operation "EnrollMember"
    Then the SOAP response should be successful
    And I capture SOAP element "memberId" as "memberId"
    Given I have a valid GetMemberStatusRequest SOAP request
    And I set SOAP element "memberId" to "{memberId}"
    When I call SOAP operation "GetMemberStatus"
    Then the SOAP response should be successful
    And the SOAP response element "tier" should equal "BASIC"
```

---

## 16) Anti-patterns (avoid)

- "Then I verify everything is correct" (too vague)
- Hard-coded IDs for GET/PUT/PATCH/DELETE in shared environments
- Repeating 10 near-identical steps instead of parameterizing
- Mixing JSON and XML steps in the same scenario
- Using JSON assertions for XML responses
- Forgetting SOAPAction header for SOAP operations

---

## 17) Quick mapping: Story → Feature → Scenario

### JSON REST
- **Story:** "As a user, I can create a booking"
- **Feature file:** `BookingController.feature`
- **Scenario:** `@id=BOOKING-010 Create and retrieve a booking`
- **Automation:** Step defs + RestAssured client calls

### XML REST
- **Story:** "As a passenger, I can check in my baggage"
- **Feature file:** `BaggageController.feature`
- **Scenario:** `@id=BAGGAGE-010 Check in and track baggage`
- **Automation:** Step defs + XmlApiClient calls

### SOAP
- **Story:** "As a customer, I can enroll in the loyalty program"
- **Feature file:** `LoyaltyService.feature`
- **Scenario:** `@id=LOYALTY-010 Enroll member and check status`
- **Automation:** Step defs + SoapClient calls
