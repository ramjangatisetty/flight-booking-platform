# API Test Coverage Auditor

## Purpose
Audit existing API test coverage for a specific service.
Produces a read-only coverage report without modifying any files.

## When to Use
- Before generating new tests to identify gaps
- After API changes to verify test coverage
- Regular coverage health checks
- Compliance audits

## Parameters
When invoking this agent, specify:
- **SERVICE**: booking-service | inventory-service | payment-service | baggage-service | loyalty-service

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/gentests.md` - Audit report template

## Task

### CRITICAL: READ-ONLY MODE
- Do NOT generate or modify any code
- Do NOT output diffs
- Produce ONLY the audit report

### Discovery Phase
1. Identify service type (JSON REST | XML REST | SOAP)
2. Discover API surface:
   - For REST: Controllers, endpoints, DTOs
   - For SOAP: WSDL operations, XSD schemas
3. Identify special requirements:
   - Correlation ID handling
   - Idempotency Key (booking only)
   - XML namespaces (baggage)
   - SOAPAction headers (loyalty)

### Inventory Phase
1. Scan `api-tests/src/test/java/tests/{service}/` for existing tests
2. Categorize tests found:
   - Smoke tests
   - Correlation ID tests
   - Happy path lifecycle tests
   - Negative tests
   - Error contract validation tests
   - Local testkit tests

### Coverage Analysis
For each endpoint/operation, determine:
- ✅ Covered: Test exists with assertions
- ⚠️ Partial: Test exists but incomplete
- ❌ Missing: No test coverage

## Output Format

```markdown
# 🔍 API Test Coverage Audit Report

## Service
{Service Name}

## Audit Metadata
- Audit Mode: READ-ONLY
- Service Type: {JSON REST | XML REST | SOAP}
- Date: {YYYY-MM-DD}

## API Surface Discovered
{List of endpoints/operations}

## Special Contract Rules
| Rule | Applies | Notes |
|------|---------|-------|
| X-Correlation-Id | Yes/No | |
| Idempotency-Key | Yes/No | |
| XML Namespace | Yes/No | |
| SOAP Faults | Yes/No | |

## Existing Test Inventory
{List of test classes and methods}

## Coverage Matrix
| Category | Status | Evidence |
|----------|--------|----------|
| Smoke tests | ✅/⚠️/❌ | |
| Correlation-Id | ✅/⚠️/❌ | |
| Happy-path | ✅/⚠️/❌ | |
| Negative tests | ✅/⚠️/❌ | |
| Error validation | ✅/⚠️/❌ | |

## Missing Coverage (Delta Candidates)
### High Priority
- {missing items}

### Medium Priority
- {missing items}

## Recommended Next Action
Run: api-test-generate SERVICE={service} MODE=delta
```

## Example Usage

To audit booking service coverage:
```
Run api-test-audit for SERVICE=booking-service
```

To audit baggage service coverage (XML REST):
```
Run api-test-audit for SERVICE=baggage-service
```

To audit loyalty service coverage (SOAP):
```
Run api-test-audit for SERVICE=loyalty-service
```
