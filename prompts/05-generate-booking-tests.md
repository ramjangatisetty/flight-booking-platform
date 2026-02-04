# Prompt 05 — Generate Booking Service Tests (JSON REST)

Follow `TEST_GENERATION_BLUEPRINT.md`.
Follow `00-agent-operating-rules.md`.

Service Type: **JSON REST**
Contract Source: OpenAPI snapshot or controller code

## Task

1) Create `framework/facade/BookingApi` using ApiClient (JSON REST).
2) Create request builders:
    - BookingRequestBuilder.valid()
    - BookingRequestBuilder.withFlightId(String)
    - BookingRequestBuilder.withPassengerId(String)
    - BookingRequestBuilder.withSeatClass(String)
3) Create response models (if not already in framework/models):
    - BookingResponse
    - BookingStatusResponse
    - LoyaltyAccrualResponse
4) Create validator `BookingValidator`.

## Generate tests in `api-tests/src/test/java/tests/booking/BookingControllerTests`:

### Smoke Tests
- GET /api-docs should return 200

### Correlation ID Tests
- POST /bookings with X-Correlation-Id header
    - Verify response header echoes the same correlation ID
- GET /bookings/{id} with X-Correlation-Id header
    - Verify response header echoes the same correlation ID
- Error response should include correlationId field

### Happy Path Tests
- Create booking (POST /bookings)
    - MUST include X-Correlation-Id header
    - Verify 201 Created response
    - Verify response contains bookingId, status=PENDING_PAYMENT
    - Capture bookingId for subsequent tests

- Get booking (GET /bookings/{id})
    - Use captured bookingId
    - Verify 200 response
    - Verify response matches created booking

- Get booking status (GET /bookings/{id}/status)
    - Use captured bookingId
    - Verify 200 response
    - Verify response contains status field

- Get loyalty accrual (GET /bookings/{id}/loyalty)
    - Use captured bookingId
    - Verify 200 response (or appropriate status if not yet accrued)

### Negative Tests
- POST /bookings with invalid JSON
    - Send malformed JSON body
    - Expect 400 Bad Request
    - Validate ErrorResponse contract

- POST /bookings with missing required fields
    - Send empty object {}
    - Expect 400 Bad Request
    - Validate ErrorResponse lists missing fields

- GET /bookings/{nonExistentId}
    - Use random UUID
    - Expect 404 Not Found
    - Validate ErrorResponse contract

- GET /bookings/{id}/status with non-existent ID
    - Use random UUID
    - Expect 404 Not Found
    - Validate ErrorResponse contract

### Idempotency Tests
- Note: Booking service does not currently enforce Idempotency-Key header

## API Endpoints Reference

| Method | Path | Request | Response | Headers |
|--------|------|---------|----------|---------|
| POST | /bookings | CreateBookingRequest | BookingResponse | X-Correlation-Id |
| GET | /bookings/{bookingId} | - | BookingResponse | X-Correlation-Id |
| GET | /bookings/{bookingId}/status | - | BookingStatusResponse | X-Correlation-Id |
| GET | /bookings/{bookingId}/loyalty | - | LoyaltyAccrualResponse | X-Correlation-Id |

## Request/Response Models

### CreateBookingRequest
```json
{
  "flightId": "FL123",
  "seatClass": "ECONOMY",
  "amount": 299.99,
  "currency": "USD",
  "memberId": "uuid (optional)"
}
```

**IMPORTANT**: The `memberId` field is an integration point with Loyalty Service.
When provided, the booking service will call Loyalty SOAP to accrue points after confirmation.

### BookingResponse
```json
{
  "bookingId": "uuid",
  "correlationId": "uuid",
  "flightId": "FL123",
  "seatClass": "ECONOMY",
  "amount": 299.99,
  "currency": "USD",
  "status": "PENDING_PAYMENT",
  "memberId": "uuid (if provided)",
  "loyaltyAccrualStatus": "SUCCEEDED|FAILED|NONE|null",
  "loyaltyPoints": 150,
  "bagTag": "XX12345678 (populated after baggage auto-creation)"
}
```

**IMPORTANT**: The `bagTag` field is populated via event integration with Baggage Service.
When booking is CONFIRMED, Baggage Service auto-creates baggage and sends back the bagTag.

## Booking Status Values
- `PENDING_PAYMENT`: Initial state after booking creation
- `CONFIRMED`: Terminal state, inventory reserved and payment succeeded
- `REJECTED`: Terminal state, inventory unavailable or payment failed

## Loyalty Accrual Status Values
- `null`: No memberId provided, accrual not attempted
- `NONE`: No memberId provided (explicit)
- `REQUESTED`: Accrual in progress
- `SUCCEEDED`: Points accrued successfully
- `FAILED`: Accrual failed

====================================================================
CROSS-SERVICE INTEGRATION TESTS (MANDATORY)
====================================================================

**CRITICAL**: The booking service has integration points with Loyalty and Baggage services.
These MUST be tested in addition to the standard API tests.

### Integration Point 1: Loyalty Service (memberId)
The `memberId` field in CreateBookingRequest triggers loyalty points accrual.

**Required Tests** (in `tests/e2e/LoyaltyBookingIntegrationTest.java`):
- Create booking WITH memberId → verify loyaltyAccrualStatus = SUCCEEDED
- Create booking WITHOUT memberId → verify loyaltyAccrualStatus = NONE
- Verify loyaltyPoints populated in booking response

### Integration Point 2: Baggage Service (bagTag)
When booking is CONFIRMED, Baggage Service auto-creates baggage via event.

**Required Tests** (in `tests/e2e/BaggageBookingIntegrationTest.java`):
- Create booking → wait for CONFIRMED → verify bagTag populated
- Verify baggage trackable via Baggage Service API

## Constraints
- Use RestAssuredApiClient (JSON REST)
- Content-Type: application/json
- Correlation ID must be generated per request (use CorrelationIdSupport)
- All negative tests must validate ErrorResponse contract
- Output diffs only
