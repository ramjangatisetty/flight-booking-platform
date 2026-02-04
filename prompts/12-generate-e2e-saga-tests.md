# Prompt 12 — Generate E2E Saga Tests

Follow `TEST_GENERATION_BLUEPRINT.md`.
Follow `00-agent-operating-rules.md`.

Service Type: **Cross-Service E2E**
Contract Source: Controller code + Kafka events + SOAP WSDL

## Task

Generate end-to-end saga flow tests that verify the complete booking workflow across ALL services, including cross-service integrations.

## Prerequisites

- All services running (booking:8081, inventory:8082, payment:8083, loyalty:8084, baggage:8085)
- Kafka running for event propagation
- PostgreSQL databases available

====================================================================
MANDATORY CROSS-SERVICE DISCOVERY (DO THIS FIRST)
====================================================================

Before generating tests, you MUST discover all cross-service integration points:

### Step 1: Scan DTOs for Foreign Key References
Read ALL request/response DTOs and identify fields that reference other services:
- `memberId` in CreateBookingRequest → Loyalty Service integration
- `bookingId` in BaggageCheckinRequest → Booking Service integration
- `bagTag` in BookingResponse → Baggage Service integration

### Step 2: Read Event Listeners
For each service, read:
- `services/{service}-service/src/main/java/**/messaging/consumer/*Listener.java`
Document what events each service consumes and what actions are triggered.

### Step 3: Read Event Publishers
For each service, read:
- `services/{service}-service/src/main/java/**/messaging/producer/*Publisher.java`
Document what events each service publishes.

### Step 4: Map Complete Flows
Document the complete flow for each business scenario:
```
Booking Saga: Booking → Inventory → Payment → Booking (status update)
Loyalty Flow: Booking (with memberId) → Loyalty SOAP → Booking (points update)
Baggage Flow: Booking (confirmed) → Baggage (auto-create) → Booking (bagTag update)
```

====================================================================
REQUIRED E2E TEST SCENARIOS
====================================================================

## Generate tests in `api-tests/src/test/java/tests/e2e/`:

### 1. BookingSagaE2ETest.java (Existing)
- Happy Path: Seed inventory → Create booking → Poll for CONFIRMED
- Failure Path: Create booking without inventory → Poll for REJECTED

### 2. LoyaltyBookingIntegrationTest.java (NEW - REQUIRED)
- **shouldAccrueLoyaltyPointsWhenBookingConfirmed**:
  1. Enroll member via Loyalty SOAP (EnrollMember)
  2. Seed inventory
  3. Create booking WITH memberId
  4. Wait for CONFIRMED status
  5. Verify loyaltyAccrualStatus = SUCCEEDED in booking response
  6. Verify points balance via Loyalty SOAP (GetMemberStatus)

- **shouldNotAccruePointsWhenNoMemberId**:
  1. Create booking WITHOUT memberId
  2. Wait for CONFIRMED
  3. Verify loyaltyAccrualStatus = NONE

### 3. BaggageBookingIntegrationTest.java (NEW - REQUIRED)
- **shouldAutoCreateBaggageWhenBookingConfirmed**:
  1. Seed inventory
  2. Create booking
  3. Wait for CONFIRMED
  4. Wait for baggage auto-creation (event processing)
  5. Verify bagTag populated in booking response
  6. Verify baggage trackable via Baggage Service

### 4. FullJourneyE2ETest.java (NEW - OPTIONAL)
- Complete flow: Loyalty enrollment → Booking with memberId → Inventory → Payment → Baggage → Verify all integrations

## Booking Status Values
- `PENDING_PAYMENT`: Initial state after booking creation
- `CONFIRMED`: Terminal state, inventory reserved and payment succeeded
- `REJECTED`: Terminal state, inventory unavailable or payment failed

## Loyalty Accrual Status Values
- `null`: No memberId provided
- `NONE`: No memberId provided (explicit)
- `REQUESTED`: Accrual in progress
- `SUCCEEDED`: Points accrued successfully
- `FAILED`: Accrual failed

====================================================================
SERVICE INTERACTION MAP
====================================================================

```
┌─────────────────────────────────────────────────────────────────────┐
│                        BOOKING SERVICE (8081)                        │
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
│  (8082)     │    │     (8083)      │    │    (8085)       │
└─────────────┘    └─────────────────┘    └─────────────────┘
                                                  │
                                                  ▼
                                         ┌─────────────────┐
                                         │    LOYALTY      │
                                         │    (8084/SOAP)  │
                                         └─────────────────┘
```

## Polling Strategy
- Poll interval: 500ms
- Max attempts: 20 (10 seconds total)
- Terminal states: `CONFIRMED`, `REJECTED`

## API Endpoints Used

### Inventory Service (Port 8082)
| Method | Path | Purpose |
|--------|------|---------|
| POST | /inventory/admin/seed | Seed inventory for testing |
| POST | /inventory/admin/reset | Reset demo state |

### Booking Service (Port 8081)
| Method | Path | Purpose |
|--------|------|---------|
| POST | /bookings | Create booking |
| GET | /bookings/{id}/status | Poll booking status |

## Seed Request Model
```json
{
  "flightId": "E2E-FL001",
  "seatClass": "ECONOMY",
  "availableSeats": 10
}
```

## Test Groups
- `e2e`: All end-to-end tests
- `saga`: Saga-specific tests

## Constraints
- Use RestAssuredApiClient for both services
- Use CorrelationIdSupport for tracing
- Use ReportLogger for ExtentReports integration
- Tests must be idempotent (use unique flight IDs per test run)
- Output diffs only

## SoapResponse API (for Loyalty Integration)
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
