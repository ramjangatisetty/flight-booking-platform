# Prompt 11 — Generate Loyalty Service Tests (SOAP Web Service)

Follow `TEST_GENERATION_BLUEPRINT.md`.

## Overview
The Loyalty Service exposes a SOAP web service at `/ws` endpoint.
Contract is defined in WSDL/XSD files:
- `services/loyalty-service/src/main/resources/wsdl/loyalty.wsdl`
- `services/loyalty-service/src/main/resources/xsd/loyalty.xsd`

## SOAP Namespace
All requests and responses use namespace: `http://letzautomate.com/loyalty/v1`

## SOAP Endpoint
- URL: `http://localhost:8085/ws` (or BASE_URL_LOYALTY + /ws)
- Binding: Document/Literal

## Task

1) Create `framework/facade/LoyaltySoapApi` using SoapClient.
2) Create SOAP request builders:
    - EnrollMemberSoapBuilder.valid()
    - GetMemberStatusSoapBuilder.valid()
    - AccruePointsSoapBuilder.valid()
3) Create SOAP response models:
    - EnrollMemberResponse
    - GetMemberStatusResponse
    - AccruePointsResponse
    - LoyaltyFault
4) Create validator `LoyaltySoapValidator`.

## Generate tests in `api-tests/src/test/java/tests/loyalty/LoyaltySoapTests`:

### Happy Path Tests

#### EnrollMember Operation
- Send valid EnrollMemberRequest
- Verify response contains memberId, tier (BASIC), status (ACTIVE)
- SOAPAction: `http://letzautomate.com/loyalty/v1/EnrollMember`

#### GetMemberStatus Operation
- First enroll a member, capture memberId
- Send GetMemberStatusRequest with captured memberId
- Verify response contains correct tier and pointsBalance
- SOAPAction: `http://letzautomate.com/loyalty/v1/GetMemberStatus`

#### AccruePoints Operation
- First enroll a member, capture memberId
- Send AccruePointsRequest with memberId, bookingId, amount
- Verify pointsCredited and newPointsBalance in response
- SOAPAction: `http://letzautomate.com/loyalty/v1/AccruePoints`

### Negative Tests (SOAP Faults)

#### Non-existent Member
- Send GetMemberStatusRequest with random UUID
- Expect SOAP Fault response
- Validate LoyaltyFault contains faultCode and faultMessage

#### Duplicate Email Enrollment
- Enroll member with email
- Attempt to enroll again with same email
- Expect SOAP Fault response
- Validate faultMessage indicates duplicate

### Admin REST Endpoints
The Loyalty Service also has REST admin endpoints:
- POST /loyalty/admin/seed - Seeds demo members
- POST /loyalty/admin/reset - Resets all members

Generate tests for these using standard JSON ApiClient.

## SOAP Request Examples

### EnrollMemberRequest
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:loy="http://letzautomate.com/loyalty/v1">
    <soap:Body>
        <loy:EnrollMemberRequest>
            <loy:firstName>John</loy:firstName>
            <loy:lastName>Doe</loy:lastName>
            <loy:email>john.doe@example.com</loy:email>
        </loy:EnrollMemberRequest>
    </soap:Body>
</soap:Envelope>
```

### GetMemberStatusRequest
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:loy="http://letzautomate.com/loyalty/v1">
    <soap:Body>
        <loy:GetMemberStatusRequest>
            <loy:memberId>uuid-here</loy:memberId>
        </loy:GetMemberStatusRequest>
    </soap:Body>
</soap:Envelope>
```

### AccruePointsRequest
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:loy="http://letzautomate.com/loyalty/v1">
    <soap:Body>
        <loy:AccruePointsRequest>
            <loy:memberId>uuid-here</loy:memberId>
            <loy:bookingId>uuid-here</loy:bookingId>
            <loy:amount>250.00</loy:amount>
            <loy:currency>USD</loy:currency>
            <loy:correlationId>uuid-here</loy:correlationId>
        </loy:AccruePointsRequest>
    </soap:Body>
</soap:Envelope>
```

## SOAP Fault Example
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <soap:Fault>
            <faultcode>soap:Server</faultcode>
            <faultstring>Member not found</faultstring>
            <detail>
                <loy:LoyaltyFault xmlns:loy="http://letzautomate.com/loyalty/v1">
                    <loy:faultCode>MEMBER_NOT_FOUND</loy:faultCode>
                    <loy:faultMessage>Member not found: uuid-here</loy:faultMessage>
                </loy:LoyaltyFault>
            </detail>
        </soap:Fault>
    </soap:Body>
</soap:Envelope>
```

## Tier Progression Rules
- BASIC: 0-4999 points
- SILVER: 5000-14999 points
- GOLD: 15000-49999 points
- PLATINUM: 50000+ points

## Idempotency
- AccruePoints is idempotent on bookingId
- Duplicate accrual requests return same result without re-crediting

## Constraints
- Use SoapClient for SOAP operations
- Use SoapEnvelopeBuilder for request construction
- Use SoapResponseParser for response extraction
- Handle SOAP faults with SoapFaultAsserter
- Use standard ApiClient for REST admin endpoints
- Output diffs only

## SoapResponse API (MANDATORY)
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
