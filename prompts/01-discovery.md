# Prompt 01 — Repository API Discovery (Read-only)

Go through the codebase in read-only mode.

Goal: Produce a structured inventory of the API surface for each service AND cross-service integration points.

Do NOT generate code. Do NOT modify files.

====================================================================
CONTRACT DISCOVERY PRIORITY (MANDATORY)
====================================================================

## For JSON REST Services (booking-service, inventory-service, payment-service)
1) OpenAPI snapshot (preferred), if present:
   - `api-tests/src/test/resources/openapi-snapshots/<service>/openapi.json`
2) Runtime `/api-docs` (ONLY if explicitly allowed for this run)
3) Controller + DTO code (fallback)

## For XML REST Services (baggage-service)
1) OpenAPI snapshot (preferred), if present:
   - `api-tests/src/test/resources/openapi-snapshots/baggage-service/openapi.json`
2) XSD schema embedded in DTOs (check @JacksonXmlRootElement annotations)
3) Controller + DTO code (fallback)

## For SOAP Services (loyalty-service)
1) WSDL/XSD snapshot (preferred), if present:
   - `api-tests/src/test/resources/wsdl-snapshots/loyalty-service/loyalty.wsdl`
   - `api-tests/src/test/resources/wsdl-snapshots/loyalty-service/loyalty.xsd`
2) Service WSDL/XSD files:
   - `services/loyalty-service/src/main/resources/wsdl/loyalty.wsdl`
   - `services/loyalty-service/src/main/resources/xsd/loyalty.xsd`
3) Endpoint + DTO code (fallback)

In your output, clearly state for each service:
- `Contract source used: snapshot | runtime | code | wsdl`
- `Service type: JSON REST | XML REST | SOAP`

====================================================================
CROSS-SERVICE INTEGRATION DISCOVERY (MANDATORY)
====================================================================

**CRITICAL**: Before completing discovery, you MUST identify cross-service integration points.

## Step 1: Scan DTOs for Foreign Key References
For each service, scan ALL request/response DTOs for fields that reference other services:

| Field Pattern | Likely Integration |
|---------------|-------------------|
| `memberId`, `loyaltyId` | Loyalty Service (SOAP) |
| `bookingId` | Booking Service (REST) |
| `reservationId` | Inventory Service (REST) |
| `bagTag`, `baggageId` | Baggage Service (XML REST) |
| `paymentId`, `transactionId` | Payment Service (REST) |

**IMPORTANT**: Optional fields often represent integration points - DO NOT IGNORE THEM.

## Step 2: Read Event Listeners
For each service, read the event listeners to understand incoming integrations:
- `services/{service}-service/src/main/java/**/messaging/consumer/*Listener.java`
- Document: which events from OTHER services this service consumes
- Document: what actions are triggered by those events

## Step 3: Read Event Publishers
For each service, read the event publishers to understand outgoing integrations:
- `services/{service}-service/src/main/java/**/messaging/producer/*Publisher.java`
- Document: which events this service publishes
- Document: which other services consume these events

## Step 4: Map Cross-Service Flows
Document complete flows that span multiple services:
- Example: Booking → Inventory → Payment → Booking (saga)
- Example: Booking → Loyalty (SOAP call for points accrual)
- Example: Booking → Baggage (event-driven auto-creation)

====================================================================
JSON REST SERVICES DISCOVERY
====================================================================

For each JSON REST service (booking-service, inventory-service, payment-service):

1) Confirm OpenAPI is enabled:
    - springdoc dependency presence
    - OpenApiConfig class location
    - configured docs path (/api-docs) and swagger UI path if configured

2) List controllers:
    - controller class name and package
    - base @RequestMapping
    - endpoints: HTTP method + path + request DTO + response DTO
    - Content-Type: application/json
    - note special headers per endpoint (X-Correlation-Id, Idempotency-Key)

3) List API DTOs:
    - request DTOs
    - response DTOs
    - ErrorResponse shape and location

4) Identify exception handler:
    - RestExceptionHandler or GlobalExceptionHandler location
    - status code mapping hints if discoverable

5) Identify filters:
    - CorrelationIdFilter behavior and where correlationId is echoed (header/body)
    - FaultInjectionFilter behavior (local only)

6) Identify local test endpoints (LocalTestController):
    - /test/reset
    - /test/failures
    - /test/events
    - which services implement events endpoint

====================================================================
XML REST SERVICES DISCOVERY
====================================================================

For baggage-service (XML REST):

1) Confirm XML support is enabled:
    - jackson-dataformat-xml dependency presence
    - @JacksonXmlRootElement annotations on DTOs

2) List controllers:
    - controller class name and package
    - base @RequestMapping
    - endpoints: HTTP method + path + request DTO + response DTO
    - Content-Type: application/xml
    - Accept: application/xml
    - note special headers per endpoint (X-Correlation-Id)

3) List XML DTOs:
    - request DTOs with @JacksonXmlProperty annotations
    - response DTOs with @JacksonXmlProperty annotations
    - XML namespace (e.g., http://letzautomate.com/baggage/v1)
    - XML root element names

4) Identify validation rules:
    - @Pattern annotations (e.g., bagTag pattern: [A-Z]{2}[0-9]{8})
    - @NotBlank, @NotNull constraints
    - Custom validation messages

5) Identify exception handler:
    - GlobalExceptionHandler location
    - XML error response format

6) Identify admin endpoints:
    - /baggage/admin/seed
    - Content-Type for admin endpoints (may differ from main endpoints)

====================================================================
SOAP SERVICES DISCOVERY
====================================================================

For loyalty-service (SOAP):

1) Locate WSDL and XSD files:
    - WSDL location: services/loyalty-service/src/main/resources/wsdl/loyalty.wsdl
    - XSD location: services/loyalty-service/src/main/resources/xsd/loyalty.xsd

2) Extract SOAP operations from WSDL:
    - Operation name
    - SOAPAction header value
    - Input message element
    - Output message element
    - Fault message element

3) Extract schema types from XSD:
    - Request element schemas
    - Response element schemas
    - Fault element schema (LoyaltyFault)
    - Enum types (TierType, StatusType)

4) Identify SOAP endpoint:
    - Endpoint URL (e.g., /ws)
    - Binding style (document/literal)
    - Transport (HTTP)

5) Identify namespace:
    - Target namespace (http://letzautomate.com/loyalty/v1)
    - SOAP envelope namespace

6) Identify REST admin endpoints (if any):
    - /loyalty/admin/seed
    - /loyalty/admin/reset
    - These use JSON, not SOAP

====================================================================
OUTPUT FORMAT
====================================================================

Output:
- One section per service module
- Clearly indicate service type (JSON REST | XML REST | SOAP)
- For SOAP services, list operations instead of endpoints
- Keep it concise and structured
- Do not propose improvements

Example output structure:

```
## booking-service (JSON REST)
Contract source used: code
Service type: JSON REST

### Controllers
- BookingController: /bookings
  - POST /bookings (CreateBookingRequest → BookingResponse)
    Headers: X-Correlation-Id, Idempotency-Key
  - GET /bookings/{id} (→ BookingResponse)
  - GET /bookings/{id}/status (→ BookingStatusResponse)

### DTOs
- CreateBookingRequest: flightId, passengerId, seatClass
- BookingResponse: id, flightId, passengerId, status, price
- ErrorResponse: timestamp, status, error, message, path

---

## baggage-service (XML REST)
Contract source used: code
Service type: XML REST
XML Namespace: http://letzautomate.com/baggage/v1

### Controllers
- BaggageController: /baggage
  - POST /baggage/checkin (BaggageCheckinRequest → BaggageCheckinResponse)
    Content-Type: application/xml
  - PUT /baggage/status/{bagTag} (BaggageStatusUpdateRequest → BaggageStatusUpdateResponse)
  - GET /baggage/track/{bagTag} (→ BaggageTrackResponse)

### XML DTOs
- BaggageCheckinRequest (root: BaggageCheckinRequest)
  - bookingId, passengerId, bagTag, origin, destination
  - bagTag pattern: [A-Z]{2}[0-9]{8}

---

## loyalty-service (SOAP)
Contract source used: wsdl
Service type: SOAP
SOAP Endpoint: /ws
Namespace: http://letzautomate.com/loyalty/v1

### SOAP Operations
- EnrollMember
  SOAPAction: http://letzautomate.com/loyalty/v1/EnrollMember
  Input: EnrollMemberRequest (firstName, lastName, email)
  Output: EnrollMemberResponse (memberId, tier, status)
  Fault: LoyaltyFault

- GetMemberStatus
  SOAPAction: http://letzautomate.com/loyalty/v1/GetMemberStatus
  Input: GetMemberStatusRequest (memberId)
  Output: GetMemberStatusResponse (memberId, tier, status, pointsBalance)
  Fault: LoyaltyFault

### REST Admin Endpoints
- POST /loyalty/admin/seed (JSON)
- POST /loyalty/admin/reset (JSON)
```

====================================================================
CROSS-SERVICE INTEGRATION OUTPUT
====================================================================

After documenting each service, add a cross-service integration section:

```
## Cross-Service Integration Points

### Integration Point: Booking → Loyalty
- Field: `memberId` in CreateBookingRequest (optional)
- Mechanism: Sync SOAP call to Loyalty Service
- Flow: When booking is CONFIRMED with memberId, BookingService calls LoyaltySoapClient.accruePoints()
- Test Required: Create booking with memberId → verify loyaltyAccrualStatus = SUCCEEDED

### Integration Point: Booking → Baggage
- Event: booking.confirmed.v1
- Consumer: BaggageService.BookingEventsListener
- Action: Auto-creates baggage for confirmed booking
- Response Event: baggage.events (baggage.checked_in.v1)
- Test Required: Create booking → wait for CONFIRMED → verify bagTag populated

### Integration Point: Baggage → Booking
- Event: baggage.events (baggage.checked_in.v1)
- Consumer: BookingService.BaggageEventsListener
- Action: Updates booking with bagTag
- Test Required: Verify bagTag in booking response after baggage checkin

### Event Flow Diagram
```
Booking Service                    Other Services
     │
     ├──► inventory.reserve.requested.v1 ──► Inventory Service
     │                                              │
     │◄── inventory.reserved.v1 ◄──────────────────┘
     │
     ├──► payment.requested.v1 ──────────► Payment Service
     │                                              │
     │◄── payment.succeeded.v1 ◄───────────────────┘
     │
     ├──► booking.confirmed.v1 ──────────► Baggage Service
     │                                              │
     │◄── baggage.checked_in.v1 ◄──────────────────┘
     │
     └──► SOAP: AccruePoints ────────────► Loyalty Service
```
```
