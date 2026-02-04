# Prompt 07 — Generate Inventory Service Tests (JSON REST)

Follow `TEST_GENERATION_BLUEPRINT.md`.
Follow `00-agent-operating-rules.md`.

Service Type: **JSON REST**
Contract Source: OpenAPI snapshot or controller code

## Task

1) Create `framework/facade/InventoryApi` using ApiClient (JSON REST).
2) Create request builders:
    - InventoryRequestBuilder.seedRequest(String flightId, String seatClass, int availableSeats)
3) Create response models (if not already in framework/models):
    - InventoryReservationResponse
    - InventoryItemResponse
4) Create validator `InventoryValidator`.

## Generate tests in `api-tests/src/test/java/tests/inventory/InventoryControllerTests`:

### Smoke Tests
- GET /api-docs should return 200

### Correlation ID Tests
- GET /inventory/reservations/{reservationId} with X-Correlation-Id header
    - Verify response header echoes the same correlation ID
- Error response should include correlationId field

### Admin Endpoint Tests
- POST /inventory/admin/seed
    - Seed inventory with valid data
    - Verify response contains created inventory item
    - Capture flightId for subsequent tests

- POST /inventory/admin/reset
    - Reset demo state
    - Verify 200 response

### Query Endpoint Tests
- GET /inventory/reservations/{reservationId}
    - Requires a valid reservationId (may need to create booking first to generate reservation)
    - Verify 200 response
    - Verify response contains: bookingId, reservationId, status, flightId, seatClass

- GET /inventory/reservations/by-booking/{bookingId}
    - Requires a valid bookingId with reservation
    - Verify 200 response
    - Verify response matches reservation data

### Negative Tests
- GET /inventory/reservations/{nonExistentId}
    - Use random UUID
    - Expect 404 Not Found
    - Validate ErrorResponse contract

- GET /inventory/reservations/by-booking/{nonExistentBookingId}
    - Use random UUID
    - Expect 404 Not Found
    - Validate ErrorResponse contract

- POST /inventory/admin/seed with invalid data
    - Send empty or malformed request
    - Expect 400 Bad Request
    - Validate ErrorResponse contract

- POST /inventory/admin/seed with negative availableSeats
    - Expect 400 Bad Request
    - Validate ErrorResponse contract

## API Endpoints Reference

| Method | Path | Request | Response | Headers |
|--------|------|---------|----------|---------|
| GET | /inventory/reservations/{reservationId} | - | InventoryReservationResponse | X-Correlation-Id |
| GET | /inventory/reservations/by-booking/{bookingId} | - | InventoryReservationResponse | X-Correlation-Id |
| POST | /inventory/admin/seed | SeedRequest | InventoryItemEntity | - |
| POST | /inventory/admin/reset | - | void | - |

## Request/Response Models

### SeedRequest
```json
{
  "flightId": "FL123",
  "seatClass": "ECONOMY",
  "availableSeats": 100
}
```

### InventoryReservationResponse
```json
{
  "bookingId": "uuid",
  "reservationId": "uuid",
  "status": "RESERVED",
  "reason": null,
  "flightId": "FL123",
  "seatClass": "ECONOMY",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

## Constraints
- Use RestAssuredApiClient (JSON REST)
- Content-Type: application/json
- Correlation ID should be included in requests (use CorrelationIdSupport)
- All negative tests must validate ErrorResponse contract
- Output diffs only
