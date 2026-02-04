# API Discovery Agent

## Purpose
Discover and document the API surface for all services.
Produces a structured inventory without generating any code.

## When to Use
- Initial project setup to understand API landscape
- After adding new services
- Before creating test framework

## Governing Documents
You MUST follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md` - Supreme authority
2. `prompts/00-agent-operating-rules.md` - Operational constraints
3. `prompts/00-security-guardrails.md` - Security policies
4. `prompts/01-discovery.md` - Discovery process

## Task

### CRITICAL: READ-ONLY MODE
- Do NOT generate code
- Do NOT modify files
- Produce ONLY the discovery report

### For Each Service, Discover:

#### JSON REST Services (booking, inventory, payment)
1. OpenAPI/Springdoc configuration
2. Controllers and base paths
3. Endpoints: HTTP method + path + request DTO + response DTO
4. Special headers (X-Correlation-Id, Idempotency-Key)
5. ErrorResponse schema
6. Exception handlers
7. Local testkit endpoints (/test/*)

#### XML REST Services (baggage)
1. Controllers and base paths
2. Endpoints with XML content type
3. XML DTOs with @JacksonXmlRootElement annotations
4. XML namespace (http://letzautomate.com/baggage/v1)
5. Validation patterns (bagTag: [A-Z]{2}[0-9]{8})
6. Admin endpoints

#### SOAP Services (loyalty)
1. WSDL location and content
2. XSD schemas
3. SOAP operations and SOAPAction headers
4. SOAP endpoint URL (/ws)
5. Fault schema (LoyaltyFault)
6. REST admin endpoints (if any)

## Output Format

```markdown
# API Discovery Report

## booking-service (JSON REST)
Contract source: code
Port: 8081

### Controllers
- BookingController: /bookings

### Endpoints
| Method | Path | Request | Response | Headers |
|--------|------|---------|----------|---------|
| POST | /bookings | CreateBookingRequest | BookingResponse | Idempotency-Key |
| GET | /bookings/{id} | - | BookingResponse | - |

---

## baggage-service (XML REST)
Contract source: code
Port: 8084
XML Namespace: http://letzautomate.com/baggage/v1

### Controllers
- BaggageController: /baggage

### Endpoints
| Method | Path | Content-Type | Request | Response |
|--------|------|--------------|---------|----------|
| POST | /baggage/checkin | application/xml | BaggageCheckinRequest | BaggageCheckinResponse |

---

## loyalty-service (SOAP)
Contract source: wsdl
Port: 8085
SOAP Endpoint: /ws
Namespace: http://letzautomate.com/loyalty/v1

### SOAP Operations
| Operation | SOAPAction | Input | Output | Fault |
|-----------|------------|-------|--------|-------|
| EnrollMember | .../EnrollMember | EnrollMemberRequest | EnrollMemberResponse | LoyaltyFault |

### REST Admin Endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | /loyalty/admin/seed | Seed demo data |
```

## Example Usage

To discover all services:
```
Run api-test-discovery
```
