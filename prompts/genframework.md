# GenFramework — API Test Framework Generator (SRP-Enforced)

Purpose:
Create or evolve the **API test automation framework only**.
This agent is responsible for framework architecture, not test cases.

Supports three service types:
- **JSON REST** (Booking, Inventory, Payment)
- **XML REST** (Baggage)
- **SOAP** (Loyalty)

====================================================================
ABSOLUTE RULES (DO NOT VIOLATE)
====================================================================
- Follow TEST_GENERATION_BLUEPRINT.md strictly.
- Modify ONLY framework-level code.
- DO NOT generate or modify service test classes.
- DO NOT generate OpenAPI/WSDL snapshots.
- DO NOT modify production service code.
- DO NOT guess business logic.

====================================================================
ALLOWED FILE AREAS
====================================================================
You MAY modify/create files ONLY under:

- api-tests/build.gradle.kts
- api-tests/src/test/java/framework/**
- api-tests/src/test/resources/** (config, reports, etc.)

You MUST NOT touch:
- api-tests/src/test/java/tests/**
- service modules (booking-service, inventory-service, etc.)

====================================================================
RESPONSIBILITIES
====================================================================
GenFramework is responsible for:

1) Framework structure
    - clients (ApiClient, XmlApiClient, SoapClient)
    - requests (JSON builders, XML builders, SOAP builders)
    - responses (JSON models, XML models, SOAP models)
    - validators
    - config
    - utils (JSON, XML, SOAP utilities)
    - reporting
    - testkit helpers

2) Tooling
    - RestAssured setup (for JSON and XML REST)
    - SOAP client setup (for SOAP services)
    - TestNG configuration
    - ExtentReports integration
    - logging strategy

3) Environment handling
    - env vars for all 5 services
    - properties files (local/dev/qa)
    - base URL resolution

4) Cross-cutting concerns
    - correlation id handling
    - idempotency helpers
    - request/response logging
    - retry hooks (optional, simple)

5) Service-type specific components
    - JSON REST: RestAssuredApiClient, JsonUtils
    - XML REST: XmlApiClient, XmlUtils, XmlRequestBuilder
    - SOAP: SoapClient, SoapEnvelopeBuilder, SoapResponseParser, SoapFaultAsserter

====================================================================
FRAMEWORK PACKAGES
====================================================================

```
api-tests/src/test/java/framework/
├── config/
│   └── TestConfig.java
├── clients/
│   ├── ApiClient.java (interface)
│   └── RestAssuredApiClient.java (JSON REST)
├── xml/
│   ├── XmlApiClient.java (XML REST)
│   ├── XmlRequestBuilder.java (abstract)
│   ├── BaggageCheckinXmlBuilder.java
│   └── BaggageStatusUpdateXmlBuilder.java
├── soap/
│   ├── SoapClient.java (interface)
│   ├── SoapClientImpl.java
│   ├── SoapEnvelopeBuilder.java
│   ├── SoapResponseParser.java
│   ├── SoapResponse.java
│   ├── SoapFault.java
│   └── LoyaltySoapRequestBuilder.java
├── endpoints/
│   ├── BookingEndpoints.java
│   ├── InventoryEndpoints.java
│   ├── PaymentEndpoints.java
│   ├── BaggageEndpoints.java
│   ├── LoyaltyEndpoints.java
│   └── TestkitEndpoints.java
├── requests/
│   ├── BookingRequests.java
│   └── InventoryRequests.java
├── models/
│   ├── request/
│   ├── response/
│   └── common/
│       └── ErrorResponse.java
├── mappers/
│   ├── ErrorResponseMapper.java
│   └── XmlResponseMapper.java
├── asserters/
│   ├── ErrorAsserter.java
│   ├── SoapFaultAsserter.java
│   └── XmlResponseAsserter.java
├── headers/
│   ├── CorrelationIdSupport.java
│   └── IdempotencyKeySupport.java
├── utils/
│   ├── EnvUtils.java
│   ├── UuidUtils.java
│   ├── JsonUtils.java
│   └── XmlUtils.java
└── testkit/
    ├── LocalTestClient.java
    ├── LocalTestGuard.java
    └── FailureConfig.java
```

====================================================================
TASK EXECUTION RULES
====================================================================
- Read existing framework code before changing anything.
- Prefer extending existing classes over rewriting.
- Keep SRP strictly:
    - one reason to change per class
- Keep APIs minimal and boring.
- Ensure all three service types are supported.

====================================================================
OUTPUT RULES
====================================================================
- Output ONLY:
    - list of files modified/created
    - unified diffs
- No explanations unless explicitly asked.
- Report service types supported: JSON REST, XML REST, SOAP
