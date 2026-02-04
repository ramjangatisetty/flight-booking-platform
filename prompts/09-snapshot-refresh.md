# Prompt 09 — Contract Snapshot Refresh (Manual, Safe)

Purpose:
Refresh contract snapshot files for a given service by capturing the latest spec from runtime endpoints
and saving it under the appropriate snapshot directory.

IMPORTANT:
- This prompt is intended for **manual, human-approved** execution.
- You MUST NOT update snapshots silently as part of other prompts.
- You MUST NOT modify any production code.
- You MUST show diffs only.

====================================================================
INPUTS
====================================================================
SERVICE: one of
- booking-service (JSON REST, OpenAPI enabled)
- inventory-service (JSON REST, OpenAPI enabled)
- payment-service (JSON REST, OpenAPI enabled, stateless - empty paths)
- loyalty-service (SOAP, WSDL enabled)
- baggage-service (XML REST, OpenAPI NOT configured - skip or use XSD)

BASE URL env var mapping:
- booking-service   -> BASE_URL_BOOKING (default: http://localhost:8081)
- inventory-service -> BASE_URL_INVENTORY (default: http://localhost:8082)
- payment-service   -> BASE_URL_PAYMENT (default: http://localhost:8083)
- loyalty-service   -> BASE_URL_LOYALTY (default: http://localhost:8084)
- baggage-service   -> BASE_URL_BAGGAGE (default: http://localhost:8085)

====================================================================
SERVICE TYPE HANDLING
====================================================================

## JSON REST Services (booking, inventory, payment)
- Fetch OpenAPI JSON from: `<BASE_URL_SERVICE>/v3/api-docs`
- Save to: `api-tests/src/test/resources/openapi-snapshots/<service>/openapi.json`
- Note: payment-service is stateless/event-driven, OpenAPI will have empty paths

## SOAP Services (loyalty - port 8084)
- Fetch WSDL from: `<BASE_URL_SERVICE>/ws/loyalty.wsdl`
- Save to: `api-tests/src/test/resources/wsdl-snapshots/<service>/loyalty.wsdl`
- Copy XSD from service resources:
  - Source: `services/loyalty-service/src/main/resources/xsd/loyalty.xsd`
  - Dest: `api-tests/src/test/resources/wsdl-snapshots/<service>/loyalty.xsd`

## XML REST Services (baggage - port 8085)
- **OpenAPI NOT configured** - springdoc dependency not included in build.gradle
- Contract defined by XSD: `services/baggage-service/src/main/resources/xsd/baggage.xsd`
- Copy XSD from service resources:
  - Source: `services/baggage-service/src/main/resources/xsd/baggage.xsd`
  - Dest: `api-tests/src/test/resources/xsd-snapshots/baggage-service/baggage.xsd`
- To enable OpenAPI in future: add `springdoc-openapi-starter-webmvc-ui` to baggage-service build.gradle

====================================================================
ALLOWED COMMANDS
====================================================================
You are allowed to run READ-ONLY repository commands:
- list directories, open/read files, grep/search

You are allowed to make network calls ONLY to:
- GET <BASE_URL_SERVICE>/api-docs (for REST services)
- GET <BASE_URL_SERVICE>/ws?wsdl (for SOAP services)

You are NOT allowed to run:
- builds/tests (no Gradle/Maven)
- docker
- git push
- installs

====================================================================
TASK
====================================================================
1) Read `TEST_GENERATION_BLUEPRINT.md` and follow it strictly.
2) Determine the service type (JSON REST, XML REST, or SOAP).
3) Determine the service base URL from env var (or system property) based on SERVICE.
4) Fetch contract spec based on service type:
   - JSON/XML REST: `<BASE_URL_SERVICE>/api-docs`
   - SOAP: `<BASE_URL_SERVICE>/ws?wsdl`
5) Normalize and save to appropriate location:
   - REST: `api-tests/src/test/resources/openapi-snapshots/<service>/openapi.json`
   - SOAP: `api-tests/src/test/resources/wsdl-snapshots/<service>/loyalty.wsdl`
6) For SOAP services, also copy XSD:
   - From: `services/loyalty-service/src/main/resources/xsd/loyalty.xsd`
   - To: `api-tests/src/test/resources/wsdl-snapshots/<service>/loyalty.xsd`
7) If the snapshot file already exists:
   - overwrite it with the latest content
   - output a diff of the changes
8) If the snapshot file does not exist:
   - create it and output the diff
9) Print a short summary:
   - SERVICE
   - Service type (JSON REST | XML REST | SOAP)
   - Base URL used
   - Output path(s)
   - Whether content changed (yes/no)

====================================================================
OUTPUT RULES
====================================================================
- Output ONLY diffs (unified diff format).
- Do not include verbose explanation.
- Do not modify any other files.

====================================================================
EXAMPLE SUMMARY OUTPUT
====================================================================

```
SERVICE: loyalty-service
Service type: SOAP
Base URL: http://localhost:8084
Output paths:
  - api-tests/src/test/resources/wsdl-snapshots/loyalty-service/loyalty.wsdl
  - api-tests/src/test/resources/wsdl-snapshots/loyalty-service/loyalty.xsd
Content changed: yes
```

```
SERVICE: baggage-service
Service type: XML REST (OpenAPI NOT configured)
Base URL: http://localhost:8085
Output paths:
  - api-tests/src/test/resources/xsd-snapshots/baggage-service/baggage.xsd (copied from source)
Note: OpenAPI not available - springdoc dependency not configured
```
