# Test Generation Blueprint (Enterprise)

_Last updated: 2026-02-03_

This blueprint defines **how AI agents generate, audit, and maintain API tests**
in this repository. It is the **highest authority** for all agents.

If any instruction conflicts:
**THIS FILE WINS.**

---

## 0) Purpose

- Generate **enterprise-grade black-box functional API tests**
- Isolate change using **SOLID (SRP-first) principles**
- Ensure **governance, auditability, and safety**
- Enable AI-driven test generation **without production risk**

---

## 1) Sources of Truth (Ordered)

Agents MUST use sources in this priority based on service type:

### For JSON REST Services (Booking, Inventory, Payment)

1. **Gherkin Feature Files** (Behavior truth)
    - `api-tests/src/test/resources/features/<service>/**/*.feature`

2. **OpenAPI Snapshots** (Contract truth)
    - `api-tests/src/test/resources/openapi-snapshots/<service>/openapi.json`

3. **Prompt Registry** (Configuration truth)
    - `00-prompt-registry.md` - Model versions, quality gates, change log

4. **Security Guardrails** (Security truth)
    - `00-security-guardrails.md` - Input validation, secrets detection

5. **Runtime /api-docs** (Optional, guarded)
    - Allowed ONLY when `ALLOW_NETWORK=true`

6. **Controller + DTO code** (Fallback only)

### For XML REST Services (Baggage)

1. **Gherkin Feature Files** (Behavior truth)
    - `api-tests/src/test/resources/features/baggage-service/**/*.feature`

2. **OpenAPI Snapshots** (Contract truth)
    - `api-tests/src/test/resources/openapi-snapshots/baggage-service/openapi.json`

3. **XML Schema (XSD)** (Schema validation)
    - Namespace: `http://letzautomate.com/baggage/v1`
    - Embedded in service DTOs or snapshot folder

4. **Controller + DTO code** (Fallback only)

### For SOAP Services (Loyalty)

1. **Gherkin Feature Files** (Behavior truth)
    - `api-tests/src/test/resources/features/loyalty-service/**/*.feature`

2. **WSDL/XSD Snapshots** (Contract truth)
    - `api-tests/src/test/resources/wsdl-snapshots/loyalty-service/loyalty.wsdl`
    - `api-tests/src/test/resources/wsdl-snapshots/loyalty-service/loyalty.xsd`

3. **Service WSDL/XSD** (Fallback)
    - `services/loyalty-service/src/main/resources/wsdl/loyalty.wsdl`
    - `services/loyalty-service/src/main/resources/xsd/loyalty.xsd`

4. **Prompt Registry** (Configuration truth)
    - `00-prompt-registry.md`

### For Kafka Event Tests

**MANDATORY DISCOVERY PHASE**: Before generating ANY Kafka event tests, agents MUST:

1. **Read Event Publisher** (Contract truth - REQUIRED FIRST)
    - `services/{service}-service/src/main/java/**/messaging/producer/*EventPublisher.java`
    - Extract: actual topic names, event types published

2. **Read Event Envelope** (Structure truth - REQUIRED SECOND)
    - `services/{service}-service/src/main/java/**/messaging/event/EventEnvelope.java`
    - Extract: exact JSON structure (meta/data sections, field names)

3. **Read Event Classes** (Payload truth)
    - `services/{service}-service/src/main/java/**/messaging/event/*Event.java`
    - Extract: payload field names and types

4. **Gherkin Feature Files** (Behavior truth - if available)
    - `api-tests/src/test/resources/features/{service}-service/**/*.feature`

**DO NOT ASSUME:**
- Topic names (e.g., don't assume `booking.events` exists)
- Event types (e.g., don't assume `booking.created.v1` exists)
- Event structure (e.g., don't assume flat `{ eventId, payload }` structure)

**ALWAYS VERIFY:**
- Actual topic names from `*EventPublisher.java` constants
- Actual event structure from `EventEnvelope.java`
- Actual event types from publisher method implementations

Agents MUST always declare:
- `Behavior source used`
- `Contract source used`
- `Service type: JSON REST | XML REST | SOAP`

### For Cross-Service Integration Tests (E2E)

**MANDATORY CROSS-SERVICE DISCOVERY PHASE**: Before generating E2E or integration tests, agents MUST:

1. **Identify Integration Points in DTOs** (REQUIRED FIRST)
    - Scan ALL request/response DTOs for foreign key references to other services
    - Look for: `memberId`, `bookingId`, `reservationId`, `bagTag`, etc.
    - Optional fields often represent integration points - DO NOT IGNORE THEM

2. **Read Event Listeners** (REQUIRED SECOND)
    - `services/{service}-service/src/main/java/**/messaging/consumer/*Listener.java`
    - Extract: which events from OTHER services this service consumes
    - Identify: what actions are triggered by those events

3. **Read Event Publishers** (REQUIRED THIRD)
    - `services/{service}-service/src/main/java/**/messaging/producer/*Publisher.java`
    - Extract: which events this service publishes that OTHER services consume

4. **Map Cross-Service Flows**
    - Document the complete flow: Service A → Event → Service B → Event → Service C
    - Identify all services involved in each business flow

**INTEGRATION POINTS TO LOOK FOR:**

| Field Pattern | Likely Integration |
|---------------|-------------------|
| `memberId`, `loyaltyId` | Loyalty Service (SOAP) |
| `bookingId` | Booking Service (REST) |
| `reservationId` | Inventory Service (REST) |
| `bagTag`, `baggageId` | Baggage Service (XML REST) |
| `paymentId`, `transactionId` | Payment Service (REST) |

**MANDATORY INTEGRATION TEST SCENARIOS:**

For each integration point discovered, generate tests that:
1. Create the prerequisite resource in the source service
2. Trigger the integration (e.g., create booking with memberId)
3. Wait for async processing (saga completion, event propagation)
4. Verify the integration effect in the target service

**EXAMPLE: Booking + Loyalty Integration**
```
1. Enroll member via Loyalty SOAP → get memberId
2. Create booking with memberId via Booking REST
3. Wait for booking CONFIRMED status
4. Verify loyaltyAccrualStatus = SUCCEEDED in booking response
5. Verify points balance increased via Loyalty SOAP GetMemberStatus
```

**DO NOT:**
- Test services in isolation when integration points exist
- Ignore optional fields in DTOs - they often represent integrations
- Assume a service is standalone without checking event listeners/publishers

---

## 2) Agent Model

Exactly **one agent** may operate at a time.

### GenFramework
Purpose:
- Create or evolve the shared test framework

Scope:
- `api-tests/build.gradle.kts`
- `api-tests/src/test/java/framework/**`
- `api-tests/src/test/resources/**` (config, reporting)

Forbidden:
- Service tests
- OpenAPI snapshots
- Production code

---

### GenTests
Purpose:
- Audit or generate **non-BDD** functional tests for one service

Scope:
- `api-tests/src/test/java/tests/<service>/**`
- `api-tests/src/test/resources/testdata/<service>/**`

Notes:
- Uses OpenAPI + overlays
- Does NOT read Gherkin

---

### GenSteps (BDD)
Purpose:
- Generate step definitions and glue code from Gherkin

Scope:
- `api-tests/src/test/resources/features/<service>/**`
- `api-tests/src/test/java/tests/<service>/**`

Notes:
- Gherkin is the **behavior source**
- OpenAPI validates contract assumptions

---

### SnapshotRefresh
Purpose:
- Refresh OpenAPI snapshots

Scope:
- `api-tests/src/test/resources/openapi-snapshots/**`

Forbidden:
- Test generation
- Framework changes

---

## 3) Modes (Mandatory)

Applicable to GenTests and GenSteps.

### MODE=audit
- Read-only
- NO file changes
- Produce coverage / binding reports

### MODE=delta (default)
- Add missing tests or step definitions ONLY
- No refactors
- No renames

### MODE=full
- Refactor allowed
- Strictly limited to target service

---

## 4) Gherkin Operating Rules (BDD)

### 4.1 Gherkin as Behavior Truth
- Agents MUST NOT invent behavior
- If behavior is missing:
  - Report gap in MODE=audit
  - Generate ONLY if explicitly instructed

### 4.2 Data Lifecycle (Mandatory)
For scenarios requiring IDs:
- Create resource first
- Capture ID
- Reuse ID
- Cleanup safely

### 4.3 Mandatory Gherkin Conventions
- Service tag (`@flight`, `@booking`, etc.)
- Scenario ID tag (`@id=FLIGHT-010`)
- Explicit Given/When/Then semantics

### 4.4 Headers
- Correlation ID rules enforced
- Booking Idempotency-Key enforced

---

## 5) Contract Rules (OpenAPI)

- Schema validation derives from OpenAPI
- ErrorResponse contract must be validated for all negatives
- Status codes follow:
  - Gherkin expectation first
  - Otherwise OpenAPI
  - Otherwise audit discrepancy

---

## 5.1) Contract Rules (XML REST)

For XML REST services (e.g., Baggage Service):
- Content-Type MUST be `application/xml`
- Accept header MUST be `application/xml`
- XML namespace MUST match service contract
- Schema validation derives from XSD embedded in DTOs
- XML error responses must be validated for structure
- Use XmlApiClient for all requests

---

## 5.2) Contract Rules (SOAP)

For SOAP services (e.g., Loyalty Service):
- WSDL defines operations, messages, and bindings
- XSD defines request/response element schemas
- SOAP envelope structure MUST follow WSDL binding style (document/literal)
- SOAPAction header MUST match WSDL operation definition
- SOAP faults MUST be validated against LoyaltyFault schema
- Use SoapClient for all SOAP operations
- REST admin endpoints use standard ApiClient

### SoapResponse API (MANDATORY)
When working with `SoapResponse`, use these exact method names:
```java
SoapResponse response = soapClient.sendRequest(soapAction, envelope);

// Correct method names:
response.getStatusCode()    // NOT statusCode()
response.getBody()          // NOT body()
response.getRawResponse()   // Full raw SOAP response
response.isFault()          // Check if response is a SOAP fault
response.getFault()         // Get SoapFault object if present
```

**DO NOT use Java record-style accessors** (e.g., `statusCode()`, `body()`).
**ALWAYS use JavaBean-style getters** (e.g., `getStatusCode()`, `getBody()`).

---

## 5.3) Contract Rules (Cross-Service E2E)

For end-to-end tests spanning multiple services:

**Service Interaction Map (This Application):**
```
┌─────────────────────────────────────────────────────────────────────┐
│                        BOOKING SERVICE                               │
│  - Accepts: memberId (→ Loyalty), flightId (→ Inventory)            │
│  - Publishes: inventory.reserve.requested.v1, booking.confirmed.v1  │
│  - Consumes: inventory.reserved.v1, payment.succeeded.v1,           │
│              baggage.events (baggage.checked_in.v1)                 │
│  - Updates: bagTag, loyaltyAccrualStatus, loyaltyPoints             │
└─────────────────────────────────────────────────────────────────────┘
         │                    │                      │
         ▼                    ▼                      ▼
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  INVENTORY  │    │     PAYMENT     │    │    BAGGAGE      │
│  SERVICE    │    │     SERVICE     │    │    SERVICE      │
│             │    │                 │    │                 │
│ Consumes:   │    │ Consumes:       │    │ Consumes:       │
│ inventory.  │    │ payment.        │    │ booking.        │
│ reserve.    │    │ requested.v1    │    │ confirmed.v1    │
│ requested   │    │                 │    │                 │
│             │    │ Publishes:      │    │ Publishes:      │
│ Publishes:  │    │ payment.        │    │ baggage.events  │
│ inventory.  │    │ succeeded.v1    │    │ (checked_in)    │
│ reserved.v1 │    │ payment.        │    │                 │
│ inventory.  │    │ failed.v1       │    │                 │
│ rejected.v1 │    │                 │    │                 │
└─────────────┘    └─────────────────┘    └─────────────────┘
                                                  │
                                                  ▼
                                         ┌─────────────────┐
                                         │    LOYALTY      │
                                         │    SERVICE      │
                                         │    (SOAP)       │
                                         │                 │
                                         │ Called by:      │
                                         │ Booking Service │
                                         │ (sync SOAP)     │
                                         │                 │
                                         │ Operations:     │
                                         │ - EnrollMember  │
                                         │ - GetMemberStatus│
                                         │ - AccruePoints  │
                                         └─────────────────┘
```

**Required E2E Test Scenarios:**

| Scenario | Services Involved | Test Location |
|----------|-------------------|---------------|
| Booking Saga Happy Path | Booking → Inventory → Payment | `tests/e2e/` |
| Booking Saga Rejection | Booking → Inventory (no stock) | `tests/e2e/` |
| Loyalty Points Accrual | Loyalty (SOAP) → Booking | `tests/e2e/` |
| Baggage Auto-Creation | Booking → Baggage | `tests/e2e/` |
| Full Journey | Loyalty → Booking → Inventory → Payment → Baggage | `tests/e2e/` |

---

## 6) Data & Environment Rules

- No DB access
- No broker access
- No hard-coded IDs (unless local fixtures)
- ENV must gate:
  - local-only testkit endpoints
  - destructive cleanup

---

## 7) Reporting

- ExtentReports is the standard
- Agents MUST reuse existing integration
- Scenario ID must appear in reports

---

## 8) Output Rules

Agents MUST output:
1. Agent name
2. Mode
3. Behavior source
4. Contract source
5. Files created/modified
6. Diffs or reports (as requested)

NO extra prose unless asked.

---

## 9) Governance Summary

This blueprint ensures:
- Deterministic AI behavior
- Safe automation generation
- Clear ownership boundaries
- Auditability for enterprise environments

AI acts as **infrastructure**, not an author.

## 10) Governance File Hierarchy

All agents MUST respect this hierarchy:

TEST_GENERATION_BLUEPRINT.md (Supreme Authority)
├── 00-agent-operating-rules.md (Operational constraints)
├── 00-prompt-registry.md (Version control & config)
├── 00-security-guardrails.md (Security policies)
├── GHERKIN_STYLE_GUIDE.md (BDD standards)
└── Agent-specific prompts
├── genframework.md
├── gentests.md
├── gensteps.md
└── [numbered task prompts]

When conflicting instructions exist:
1. Higher file in hierarchy wins
2. Explicit "DO NOT" beats "SHOULD"
3. Security rules are non-negotiable
