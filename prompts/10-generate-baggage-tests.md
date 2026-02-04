# Prompt 10 — Generate Baggage Service Tests (XML REST)

Follow `TEST_GENERATION_BLUEPRINT.md`.

## Overview
The Baggage Service uses XML content type (application/xml) for all endpoints.
This requires using XmlApiClient instead of the standard JSON ApiClient.

## XML Namespace
All requests and responses use namespace: `http://letzautomate.com/baggage/v1`

## Task

1) Create `framework/facade/BaggageApi` using XmlApiClient.
2) Create XML request builders:
    - BaggageCheckinXmlBuilder.valid()
    - BaggageStatusUpdateXmlBuilder.valid()
3) Create XML response models:
    - BaggageCheckinResponse
    - BaggageStatusUpdateResponse
    - BaggageTrackResponse
4) Create validator `BaggageXmlValidator`.

## Generate tests in `api-tests/src/test/java/tests/baggage/BaggageControllerTests`:

### Smoke Tests
- GET /api-docs should return 200

### Happy Path Tests
- POST /baggage/checkin with valid XML payload
    - Verify 200 response
    - Verify XML response contains bagTag, status, acceptedAt
- PUT /baggage/status/{bagTag} with valid XML payload
    - Verify 200 response
    - Verify status update reflected in response
- GET /baggage/track/{bagTag}
    - Verify 200 response
    - Verify tracking history returned

### Admin Tests
- POST /baggage/admin/seed
    - Verify demo data seeded
    - Capture bagTag for subsequent tests

### Negative Tests
- POST /baggage/checkin with invalid XML (malformed)
    - Expect 400
    - Validate error response
- POST /baggage/checkin with missing required elements
    - Expect 400
    - Validate error response lists missing fields
- GET /baggage/track/{nonExistentBagTag}
    - Expect 404
    - Validate error response
- PUT /baggage/status/{nonExistentBagTag}
    - Expect 404
    - Validate error response

### Correlation ID Tests
- Verify X-Correlation-Id header echo behavior

## XML Request Examples

### BaggageCheckinRequest
```xml
<?xml version="1.0" encoding="UTF-8"?>
<BaggageCheckinRequest xmlns="http://letzautomate.com/baggage/v1">
    <bookingId>uuid-here</bookingId>
    <passengerId>uuid-here</passengerId>
    <bagTag>AB12345678</bagTag>
    <origin>JFK</origin>
    <destination>LAX</destination>
</BaggageCheckinRequest>
```

### BaggageStatusUpdateRequest
```xml
<?xml version="1.0" encoding="UTF-8"?>
<BaggageStatusUpdateRequest xmlns="http://letzautomate.com/baggage/v1">
    <status>IN_TRANSIT</status>
    <location>CONVEYOR_BELT_3</location>
</BaggageStatusUpdateRequest>
```

## Validation Rules
- bagTag must match pattern: [A-Z]{2}[0-9]{8}
- origin/destination must be 3-letter airport codes
- status must be valid enum value

## Constraints
- Use XmlApiClient for all requests
- Set Content-Type: application/xml
- Set Accept: application/xml
- Handle XML namespaces correctly
- Output diffs only
