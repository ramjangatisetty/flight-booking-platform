# API Contract Snapshot Refresh

## Purpose
Refresh OpenAPI or WSDL contract snapshots for a specific service.
Captures the latest API contract from running services.

## When to Use
- After API changes in a service
- Initial setup to capture baseline contracts
- Before generating tests to ensure contract accuracy

## Parameters
When invoking this agent, specify:
- **SERVICE**: booking-service | inventory-service | payment-service | baggage-service | loyalty-service

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/09-snapshot-refresh.md` - Snapshot refresh process

## Prerequisites
- Target service must be running
- Network access to service endpoint

## Task

### For JSON/XML REST Services (booking, inventory, payment, baggage)
1. Fetch OpenAPI spec from: `{BASE_URL}/api-docs`
2. Save to: `api-tests/src/test/resources/openapi-snapshots/{service}/openapi.json`

### For SOAP Services (loyalty)
1. Fetch WSDL from: `{BASE_URL}/ws?wsdl`
2. Save to: `api-tests/src/test/resources/wsdl-snapshots/{service}/loyalty.wsdl`
3. Copy XSD from: `services/loyalty-service/src/main/resources/xsd/loyalty.xsd`
4. Save to: `api-tests/src/test/resources/wsdl-snapshots/{service}/loyalty.xsd`

## Base URLs
| Service | Environment Variable | Default |
|---------|---------------------|---------|
| booking-service | BASE_URL_BOOKING | http://localhost:8081 |
| inventory-service | BASE_URL_INVENTORY | http://localhost:8082 |
| payment-service | BASE_URL_PAYMENT | http://localhost:8083 |
| baggage-service | BASE_URL_BAGGAGE | http://localhost:8084 |
| loyalty-service | BASE_URL_LOYALTY | http://localhost:8085 |

## Output
Report:
- SERVICE name
- Service type (JSON REST | XML REST | SOAP)
- Base URL used
- Output path(s)
- Whether content changed (yes/no)
- Diff of changes (if any)

## Example Usage

To refresh booking service snapshot:
```
Run api-test-snapshot for SERVICE=booking-service
```

To refresh loyalty service WSDL:
```
Run api-test-snapshot for SERVICE=loyalty-service
```
