---
description: Generate or audit functional API tests for one service (audit / delta / full)
argument-hint: SERVICE=<booking-service|inventory-service|payment-service|baggage-service|loyalty-service> [MODE=audit|delta|full]
---

You are an AI agent operating in a microservices repository that contains an `api-tests` module
used for enterprise-grade black-box functional API testing.

====================================================================
NON-NEGOTIABLE RULES
====================================================================

## RELATED AGENT (BDD)
If your input is a Gherkin `.feature` file (behavior-driven specs), STOP and use:
- GenSteps SERVICE=<...> MODE=<audit|delta|full> [ALLOW_NETWORK=true|false]

GenTests is for OpenAPI/WSDL-driven functional tests (non-BDD). Do not mix the two.

====================================================================
SERVICE TYPES
====================================================================

| Service | Type | Client | Content-Type |
|---------|------|--------|--------------|
| booking-service | JSON REST | ApiClient | application/json |
| inventory-service | JSON REST | ApiClient | application/json |
| payment-service | JSON REST | ApiClient | application/json |
| baggage-service | XML REST | XmlApiClient | application/xml |
| loyalty-service | SOAP | SoapClient | text/xml |

Note: loyalty-service also has REST admin endpoints that use ApiClient.

====================================================================
RULES
====================================================================
- Read and follow `00-agent-operating-rules.md` strictly.
- Read and follow `TEST_GENERATION_BLUEPRINT.md` strictly.
- Scope strictly to the service module: $SERVICE.
- Do NOT touch tests for other services.
- You may run READ-ONLY repository inspection:
  - open/read files
  - list directories
  - search/grep
- You MUST NOT:
  - run builds, tests, gradle, curl, or network calls
  - modify production code in service modules
  - introduce JUnit (TestNG only)
  - perform DB validations
- Output rules:
  - MODE=audit → NO diffs, NO code changes
  - MODE=delta/full → OUTPUT DIFFS ONLY

====================================================================
MODE DEFINITIONS (MANDATORY)
====================================================================
- MODE=audit:
  - Read-only
  - No file modifications
  - Produce a structured audit report (see template below)
- MODE=delta (default if MODE not provided):
  - Add missing tests ONLY
  - Do NOT rewrite, rename, or delete existing tests
- MODE=full:
  - You MAY refactor, rename, or rewrite tests
  - ONLY within the $SERVICE scope
  - Do NOT touch other services

If MODE is missing, assume MODE=delta.

====================================================================
TASK FLOW (DO NOT SKIP STEPS)
====================================================================

## 1) DISCOVERY (Scoped to $SERVICE)

### For JSON REST Services (booking, inventory, payment)
- Identify controllers and base paths.
- Identify all endpoints (method + path).
- Identify request/response DTOs.
- Identify ErrorResponse schema.
- Identify special headers: X-Correlation-Id, Idempotency-Key (if applicable)
- Identify local-only testkit endpoints (/test/*).

### For XML REST Services (baggage)
- Identify controllers and base paths.
- Identify all endpoints (method + path).
- Identify XML request/response DTOs with namespaces.
- Identify XML error response format.
- Identify validation patterns (e.g., bagTag pattern).
- Identify admin endpoints.

### For SOAP Services (loyalty)
- Identify WSDL location and operations.
- Identify XSD schemas and element definitions.
- Identify SOAP endpoint URL (/ws).
- Identify SOAPAction headers for each operation.
- Identify SOAP fault schema (LoyaltyFault).
- Identify REST admin endpoints (if any).

## 2) TEST INVENTORY (Scoped)
- Inspect `api-tests/src/test/java` for tests related to $SERVICE.
- Detect coverage by category:
  A) Smoke: GET /api-docs (REST) or GET /ws?wsdl (SOAP)
  B) Correlation-Id behavior
  C) Happy-path lifecycle:
     - JSON REST: create → get → patch/put → delete
     - XML REST: checkin → update status → track
     - SOAP: enroll → get status → accrue points
  D) Negative tests:
     - invalid payload (JSON/XML/SOAP)
     - invalid id/path
     - missing required fields
  E) ErrorResponse/SoapFault contract validation
  F) Local testkit validation (ENV=local)
  G) **Cross-service integration tests** (NEW - MANDATORY)

A category is considered COVERED only if:
- There is at least one explicit test method invoking the endpoint/operation
- Assertions are present

## 3) CROSS-SERVICE INTEGRATION DISCOVERY (MANDATORY)

Before generating tests, identify integration points for $SERVICE:

### Step 1: Scan DTOs for Foreign Key References
Look for fields that reference other services:
- `memberId` → Loyalty Service integration
- `bookingId` → Booking Service integration
- `bagTag` → Baggage Service integration
- `reservationId` → Inventory Service integration

**IMPORTANT**: Optional fields often represent integration points - DO NOT IGNORE THEM.

### Step 2: Read Event Listeners
Check `services/$SERVICE/src/main/java/**/messaging/consumer/*Listener.java`:
- What events does this service consume?
- What actions are triggered?

### Step 3: Read Event Publishers
Check `services/$SERVICE/src/main/java/**/messaging/producer/*Publisher.java`:
- What events does this service publish?
- Which services consume them?

### Step 4: Generate Integration Tests
For each integration point discovered:
1. Create prerequisite resource in source service
2. Trigger the integration
3. Wait for async processing
4. Verify the integration effect

====================================================================
MODE = AUDIT
====================================================================
If MODE=audit:

- DO NOT generate or modify code.
- DO NOT output diffs.
- Produce the audit report EXACTLY using the template below.

---------------- AUDIT REPORT TEMPLATE ----------------

# 🔍 API Test Coverage Audit Report

## Service
<Service Name>

## Audit Metadata
- Audit Mode: READ-ONLY
- Generated By: GenTests Agent
- Scope: api-tests module only
- Blueprint: TEST_GENERATION_BLUEPRINT.md
- Date: <YYYY-MM-DD>
- Service Type: JSON REST | XML REST | SOAP

---

## 1️⃣ API Surface Discovered

### For JSON/XML REST Services:
| Controller | Base Path |
|-----------|-----------|
| <ControllerName> | <BasePath> |

### Endpoints
| Method | Path | Content-Type | Notes |
|------|------|--------------|------|
| GET | /example | application/json | Description |

### For SOAP Services:
| Operation | SOAPAction | Input | Output |
|-----------|------------|-------|--------|
| EnrollMember | http://...EnrollMember | EnrollMemberRequest | EnrollMemberResponse |

---

## 2️⃣ Special Contract Rules

| Rule | Applies | Notes |
|----|-------|------|
| X-Correlation-Id | Yes/No | |
| Idempotency-Key | Yes/No | |
| XML Namespace | Yes/No | namespace URL |
| SOAP Faults | Yes/No | LoyaltyFault schema |
| Local Testkit | Yes/No | |

---

## 3️⃣ Existing Test Inventory

### Test Classes Found
- <TestClass1>
- <TestClass2>

### Test Categories Detected
| Category | Covered | Evidence |
|--------|--------|---------|
| Smoke | ✅/❌ | |
| Correlation-Id | ✅/❌ | |
| Happy-path lifecycle | ✅/⚠️/❌ | |
| Negative tests | ✅/❌ | |
| ErrorResponse/SoapFault validation | ✅/❌ | |
| Local testkit | ✅/❌ | |
| **Cross-service integration** | ✅/❌ | |

---

## 4️⃣ Cross-Service Integration Points

### Integration Points Discovered
| Field/Event | Target Service | Mechanism | Test Coverage |
|-------------|---------------|-----------|---------------|
| memberId | Loyalty | SOAP call | ✅/❌ |
| bookingId | Booking | Event | ✅/❌ |
| bagTag | Baggage | Event | ✅/❌ |

### Missing Integration Tests
- <List any integration points without test coverage>

---

## 4️⃣ Coverage Matrix (Blueprint Compliance)

| Mandatory Category | Status |
|------------------|-------|
| Smoke tests | |
| Correlation-Id tests | |
| Happy-path lifecycle | |
| Negative tests | |
| Error contract validation | |
| Cleanup after create | |
| Local testkit (ENV=local) | |

Legend:
- ✅ Covered
- ⚠️ Partially covered
- ❌ Missing

---

## 5️⃣ Missing Coverage (Delta Candidates)

### High Priority
- <Missing Item>

### Medium Priority
- <Missing Item>

### Low Priority
- <Missing Item>

---

## 6️⃣ Recommended Next Action

| Mode | Description |
|----|------------|
| MODE=delta | Add missing tests only |
| MODE=full | Full standardization/refactor |
| MODE=audit | Re-run audit to verify closure |

Suggested Command:
```
GenTests SERVICE=<service> MODE=delta
```

====================================================================
SERVICE-SPECIFIC PROMPTS
====================================================================

For detailed test generation, use the service-specific prompts:
- JSON REST (Booking): `05-generate-booking-tests.md`
- JSON REST (Inventory): `07-generate-inventory-tests.md`
- XML REST (Baggage): `10-generate-baggage-tests.md`
- SOAP (Loyalty): `11-generate-loyalty-tests.md`
- Local Testkit: `08-local-testkit-tests.md`
