# Prompt 04 — Generate Flight Service Tests (Functional + Negative)

Follow `TEST_GENERATION_BLUEPRINT.md`.

Task:
1) Create framework facade `framework/facade/FlightApi` using ApiClient + endpoints.
2) Create request builders under `framework/builders`:
    - FlightRequestBuilder.valid()
    - FlightPatchRequestBuilder.validPatch()
    - FlightStatusRequestBuilder.validStatus()
3) Create service validator `framework/validators/FlightValidator` for core assertions.

Generate tests in `api-tests/src/test/java/tests/flight/FlightControllerTests`:
- Smoke:
    - /api-docs returns 200 (already exists ok)
- Correlation:
    - send X-Correlation-Id and verify echo on success OR error body
- Happy path lifecycle:
    - create flight (POST /flights) -> capture id
    - get flight (GET /flights/{id}) -> assert fields
    - patch flight (PATCH /flights/{id}) -> assert only changed fields
    - patch status (PATCH /flights/{id}/status) -> assert status
    - delete (DELETE /flights/{id}) -> assert delete response
- Negative:
    - invalid JSON -> 400 + ErrorResponseValidator
    - GET with random/non-existent id -> 404 (or actual) + ErrorResponseValidator

Notes:
- Use unique data (UUID suffix) for flight numbers if required.
- Do not assume DB state except what your create call returns.
- If local env is detected, optionally call /test/reset before tests.

Reporting rules (already implemented):
- Extent report is already configured and generated under build/reports/extent.
- Do NOT create or modify reporting classes in this prompt.
- Tests MUST NOT import ExtentReports/ExtentTest directly.
- Use the existing reporting facade/util (e.g., StepLogger / ReportManager) if present, otherwise do not add new reporting calls.
- Clients/requests/mappers/asserters must NOT write to the report.
- Continue logging HTTP request/response via the existing mechanism only (do not duplicate logging in tests).

Output diffs only.
