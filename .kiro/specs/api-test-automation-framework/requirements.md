# Requirements Document

## Introduction

This document defines the requirements for an enterprise-grade API Test Automation Framework for the Flight Booking Platform. The framework enables black-box functional testing of all microservices (Booking, Inventory, Payment, Baggage, Loyalty) through their APIs, following SOLID principles with SRP-first design. The framework supports multiple API types including JSON REST, XML REST, and SOAP web services. It supports multiple test generation modes (audit/delta/full), environment configurations (local/dev/qa), and integrates with OpenAPI contracts and WSDL/XSD schemas as sources of truth.

## Glossary

- **Framework**: The shared test automation infrastructure providing clients, utilities, and abstractions for API testing
- **ApiClient**: Interface abstraction for HTTP operations, hiding RestAssured implementation details
- **SoapClient**: Interface abstraction for SOAP web service operations
- **OpenAPI_Snapshot**: Local copy of service OpenAPI specification used as contract source of truth for REST services
- **WSDL_Snapshot**: Local copy of WSDL/XSD files used as contract source of truth for SOAP services
- **Correlation_ID**: UUID header (X-Correlation-Id) propagated across service boundaries for request tracing
- **Idempotency_Key**: Header required for booking creation to ensure exactly-once semantics
- **TestKit**: Local-only test utilities for resetting state, injecting failures, and querying events
- **ErrorResponse**: Standardized error contract returned by REST services for error conditions
- **SOAP_Fault**: Standardized fault contract returned by SOAP services for error conditions
- **Test_Mode**: Operating mode for test generation (audit=read-only, delta=add-missing, full=refactor-allowed)
- **Service_Under_Test**: One of the five microservices being tested (Booking, Inventory, Payment, Baggage, Loyalty)
- **XML_REST_Service**: REST service that uses application/xml content type (e.g., Baggage Service)
- **SOAP_Service**: Web service using SOAP protocol with WSDL contract (e.g., Loyalty Service)

## Requirements

### Requirement 1: Gradle Module Setup

**User Story:** As a test engineer, I want a dedicated Gradle module for API tests, so that test code is isolated from production code and follows enterprise governance rules.

#### Acceptance Criteria

1. THE Framework SHALL create an `api-tests` Gradle module at the repository root
2. THE Framework SHALL register the module in `settings.gradle` using the pattern `include 'api-tests'`
3. THE Framework SHALL configure dependencies for RestAssured 5.5.x, TestNG 7.10.x, AssertJ 3.26.x, and Jackson 2.17.x
4. THE Framework SHALL configure the Gradle test task to use TestNG as the test runner
5. THE Framework SHALL NOT modify any production service code or build files
6. THE Framework SHALL NOT import packages from domain or application layers of services

### Requirement 2: Framework Package Structure

**User Story:** As a test engineer, I want a well-organized framework structure following SRP principles, so that each component has a single responsibility and the codebase is maintainable.

#### Acceptance Criteria

1. THE Framework SHALL create packages under `api-tests/src/test/java/framework/` for: config, clients, endpoints, requests, models, mappers, asserters, headers, utils, testkit, xml, soap
2. THE Framework SHALL ensure the `config` package contains TestConfig for environment variable and property management
3. THE Framework SHALL ensure the `clients` package contains ApiClient interface and RestAssuredApiClient implementation for JSON REST services
4. THE Framework SHALL ensure the `endpoints` package contains constant classes for each service's endpoint paths
5. THE Framework SHALL ensure the `requests` package contains request builder classes for each service
6. THE Framework SHALL ensure the `models` package contains test-side contract models (not reused from production DTOs)
7. THE Framework SHALL ensure the `mappers` package contains response-to-model mapping utilities
8. THE Framework SHALL ensure the `asserters` package contains validation utilities for response contracts
9. THE Framework SHALL ensure the `headers` package contains utilities for Correlation ID and Idempotency Key handling
10. THE Framework SHALL ensure the `utils` package contains general utilities for environment, UUID, JSON, and XML operations
11. THE Framework SHALL ensure the `testkit` package contains LocalTestClient for local-only test operations
12. THE Framework SHALL ensure the `xml` package contains XmlApiClient for XML REST services and XML serialization utilities
13. THE Framework SHALL ensure the `soap` package contains SoapClient interface, SoapClientImpl implementation, and SOAP envelope utilities

### Requirement 3: Environment Configuration

**User Story:** As a test engineer, I want to configure tests for different environments, so that I can run tests against local, dev, or QA deployments.

#### Acceptance Criteria

1. WHEN TestConfig initializes, THE Framework SHALL read base URLs from environment variables: BASE_URL_BOOKING, BASE_URL_INVENTORY, BASE_URL_PAYMENT, BASE_URL_BAGGAGE, BASE_URL_LOYALTY
2. WHEN TestConfig initializes, THE Framework SHALL read ENV variable to determine environment (local/dev/qa)
3. WHEN TestConfig initializes, THE Framework SHALL read LOG_HTTP variable to enable/disable HTTP logging
4. IF a required base URL is missing, THEN THE Framework SHALL fail fast with a descriptive error message
5. THE Framework SHALL support default port assignments: Booking=8081, Inventory=8082, Payment=8083, Baggage=8084, Loyalty=8085
6. WHEN ENV equals "local", THE Framework SHALL enable testkit endpoints access

### Requirement 4: API Client Abstraction

**User Story:** As a test engineer, I want an abstracted API client, so that tests are decoupled from the HTTP library implementation and RestAssured usage is centralized.

#### Acceptance Criteria

1. THE ApiClient interface SHALL define methods: get(path, headers), post(path, headers, body), put(path, headers, body), patch(path, headers, body), delete(path, headers)
2. THE RestAssuredApiClient SHALL implement ApiClient and be bound to a single base URL per instance
3. THE RestAssuredApiClient SHALL be the ONLY location where RestAssured is directly used for JSON REST services
4. WHEN LOG_HTTP is true, THE RestAssuredApiClient SHALL log request and response details
5. THE Framework SHALL NOT allow direct RestAssured usage in test classes

### Requirement 5: XML REST Client Support

**User Story:** As a test engineer, I want XML REST client support, so that I can test services like Baggage Service that use application/xml content type.

#### Acceptance Criteria

1. THE XmlApiClient SHALL extend or implement ApiClient interface with XML content type support
2. THE XmlApiClient SHALL set Content-Type header to "application/xml" for requests
3. THE XmlApiClient SHALL set Accept header to "application/xml" for responses
4. THE Framework SHALL provide XmlUtils for serializing Java objects to XML strings
5. THE Framework SHALL provide XmlUtils for deserializing XML responses to Java objects
6. THE XmlApiClient SHALL support XML namespace handling as defined in service contracts
7. WHEN LOG_HTTP is true, THE XmlApiClient SHALL log XML request and response bodies

### Requirement 6: SOAP Client Support

**User Story:** As a test engineer, I want SOAP client support, so that I can test SOAP-based services like Loyalty Service.

#### Acceptance Criteria

1. THE SoapClient interface SHALL define methods for each SOAP operation: enrollMember, getMemberStatus, accruePoints
2. THE SoapClientImpl SHALL construct proper SOAP envelopes with correct namespaces
3. THE SoapClientImpl SHALL send requests to the SOAP endpoint (e.g., /ws for Loyalty Service)
4. THE SoapClientImpl SHALL parse SOAP responses and extract payload elements
5. THE SoapClientImpl SHALL handle SOAP faults and map them to test-side fault models
6. THE Framework SHALL provide SoapEnvelopeBuilder for constructing SOAP request envelopes
7. THE Framework SHALL provide SoapResponseParser for extracting data from SOAP response envelopes
8. THE SoapClient SHALL support SOAPAction header as defined in WSDL
9. WHEN LOG_HTTP is true, THE SoapClient SHALL log SOAP request and response envelopes

### Requirement 7: Contract Snapshot Management

**User Story:** As a test engineer, I want contract snapshots as the source of truth, so that tests validate against stable API contracts.

#### Acceptance Criteria

1. THE Framework SHALL create snapshot directories at `api-tests/src/test/resources/openapi-snapshots/<service>/` for REST services
2. THE Framework SHALL create snapshot directories at `api-tests/src/test/resources/wsdl-snapshots/<service>/` for SOAP services
3. THE Framework SHALL support OpenAPI snapshots for: booking-service, inventory-service, payment-service, baggage-service
4. THE Framework SHALL support WSDL/XSD snapshots for: loyalty-service
5. WHEN generating tests for REST services, THE Framework SHALL prioritize contract discovery in order: OpenAPI snapshot, runtime /api-docs (if allowed), controller code (fallback)
6. WHEN generating tests for SOAP services, THE Framework SHALL use WSDL/XSD snapshots as the contract source
7. THE Framework SHALL report which contract source was used for each test generation run

### Requirement 8: Correlation ID Testing

**User Story:** As a test engineer, I want to verify Correlation ID behavior, so that I can ensure request tracing works correctly across services.

#### Acceptance Criteria

1. THE Framework SHALL provide CorrelationIdSupport utility for generating and managing X-Correlation-Id headers
2. WHEN a REST request includes X-Correlation-Id header, THE Service_Under_Test SHALL echo the same value in the response header
3. WHEN a SOAP request includes correlationId element, THE Service_Under_Test SHALL include it in the response
4. WHEN an error occurs in REST services, THE ErrorResponse SHALL include the correlationId field matching the request header
5. THE Framework SHALL generate tests that verify Correlation ID echo behavior for each service

### Requirement 9: Booking Service Tests

**User Story:** As a test engineer, I want comprehensive tests for the Booking Service, so that I can validate booking lifecycle operations.

#### Acceptance Criteria

1. THE Framework SHALL generate smoke tests that verify GET /api-docs returns 200
2. THE Framework SHALL generate happy path tests for: POST /bookings (create), GET /bookings/{id} (retrieve), GET /bookings/{id}/status (status check)
3. WHEN creating a booking via POST /bookings, THE test SHALL include Idempotency-Key header
4. THE Framework SHALL generate negative tests for: missing Idempotency-Key (expect 4xx), invalid JSON payload (expect 400), non-existent booking ID (expect 404)
5. THE Framework SHALL validate ErrorResponse contract for all negative test scenarios
6. THE Framework SHALL generate tests that verify Correlation ID echo behavior

### Requirement 10: Inventory Service Tests

**User Story:** As a test engineer, I want comprehensive tests for the Inventory Service, so that I can validate inventory and reservation operations.

#### Acceptance Criteria

1. THE Framework SHALL generate smoke tests that verify GET /api-docs returns 200
2. THE Framework SHALL generate tests for: GET /inventory/reservations/{reservationId}, GET /inventory/reservations/by-booking/{bookingId}
3. THE Framework SHALL generate admin endpoint tests for: POST /inventory/admin/seed, POST /inventory/admin/reset
4. THE Framework SHALL generate negative tests for: non-existent reservation ID (expect 404), non-existent booking ID (expect 404)
5. THE Framework SHALL validate ErrorResponse contract for all negative test scenarios
6. THE Framework SHALL generate tests that verify Correlation ID echo behavior

### Requirement 11: Payment Service Tests

**User Story:** As a test engineer, I want tests for the Payment Service, so that I can validate payment processing simulation.

#### Acceptance Criteria

1. THE Framework SHALL generate smoke tests that verify GET /api-docs returns 200
2. THE Framework SHALL generate tests for available payment endpoints
3. THE Framework SHALL validate ErrorResponse contract for error scenarios
4. THE Framework SHALL generate tests that verify Correlation ID echo behavior

### Requirement 12: Baggage Service Tests (XML REST)

**User Story:** As a test engineer, I want tests for the Baggage Service using XML payloads, so that I can validate baggage tracking operations with XML content type.

#### Acceptance Criteria

1. THE Framework SHALL generate smoke tests that verify GET /api-docs returns 200
2. THE Framework SHALL generate tests using XmlApiClient for: POST /baggage/checkin, PUT /baggage/status/{bagTag}, GET /baggage/track/{bagTag}
3. THE Framework SHALL construct XML request payloads with correct namespace (http://letzautomate.com/baggage/v1)
4. THE Framework SHALL parse XML responses and validate against expected schema
5. THE Framework SHALL generate admin endpoint tests for: POST /baggage/admin/seed
6. THE Framework SHALL generate negative tests for: non-existent bag tag (expect 404), invalid XML payload (expect 400), missing required XML elements
7. THE Framework SHALL validate XML error responses for all negative test scenarios
8. THE Framework SHALL provide BaggageXmlRequestBuilder for constructing valid XML payloads

### Requirement 13: Loyalty Service Tests (SOAP)

**User Story:** As a test engineer, I want tests for the Loyalty Service using SOAP protocol, so that I can validate loyalty member operations via SOAP web service.

#### Acceptance Criteria

1. THE Framework SHALL generate tests using SoapClient for SOAP operations: EnrollMember, GetMemberStatus, AccruePoints
2. THE Framework SHALL construct SOAP envelopes with correct namespace (http://letzautomate.com/loyalty/v1)
3. THE Framework SHALL send requests to SOAP endpoint at /ws with correct SOAPAction headers
4. THE Framework SHALL parse SOAP responses and validate against XSD schema
5. THE Framework SHALL generate negative tests for: non-existent member ID (expect SOAP fault), duplicate email enrollment (expect SOAP fault)
6. THE Framework SHALL validate SOAP fault responses match LoyaltyFault schema (faultCode, faultMessage)
7. THE Framework SHALL provide LoyaltySoapRequestBuilder for constructing valid SOAP request payloads
8. THE Framework SHALL generate admin endpoint tests for REST admin endpoints: POST /loyalty/admin/seed, POST /loyalty/admin/reset

### Requirement 14: ErrorResponse Contract Validation

**User Story:** As a test engineer, I want standardized error response validation, so that I can ensure all services follow the same error contract.

#### Acceptance Criteria

1. THE Framework SHALL define ErrorResponse model with fields: timestamp, status, error, message, path, correlationId
2. THE ErrorAsserter SHALL validate that all required ErrorResponse fields are present
3. THE ErrorAsserter SHALL validate that status code in ErrorResponse matches HTTP response status
4. THE ErrorAsserter SHALL validate that correlationId matches the request header when provided
5. WHEN a negative test receives an error response, THE test SHALL use ErrorAsserter to validate the contract

### Requirement 15: SOAP Fault Validation

**User Story:** As a test engineer, I want standardized SOAP fault validation, so that I can ensure SOAP services return proper fault responses.

#### Acceptance Criteria

1. THE Framework SHALL define SoapFault model with fields: faultCode, faultMessage
2. THE SoapFaultAsserter SHALL validate that SOAP fault responses contain required elements
3. THE SoapFaultAsserter SHALL extract fault details from SOAP envelope fault element
4. WHEN a SOAP operation fails, THE test SHALL use SoapFaultAsserter to validate the fault contract

### Requirement 16: Local TestKit Support

**User Story:** As a test engineer, I want local-only test utilities, so that I can reset state and inject failures during local development.

#### Acceptance Criteria

1. THE LocalTestClient SHALL provide methods for: reset(), configureFailures(config), events()
2. IF ENV does not equal "local", THEN THE LocalTestClient SHALL throw an exception when invoked
3. THE Framework SHALL generate testkit tests that are guarded by ENV=local condition
4. THE Framework SHALL support POST /test/reset for state cleanup
5. THE Framework SHALL support POST /test/failures for fault injection configuration
6. THE Framework SHALL support GET /test/events for event inspection

### Requirement 17: Test Generation Modes

**User Story:** As a test engineer, I want different test generation modes, so that I can audit coverage, add missing tests, or perform full refactoring.

#### Acceptance Criteria

1. WHEN MODE equals "audit", THE Framework SHALL perform read-only analysis and produce coverage reports without modifying files
2. WHEN MODE equals "delta", THE Framework SHALL add missing tests only without refactoring existing tests
3. WHEN MODE equals "full", THE Framework SHALL allow refactoring within the scope of the target service
4. IF MODE is not specified, THEN THE Framework SHALL default to "delta" mode
5. THE Framework SHALL report: agent used, mode used, contract source used, files created/modified

### Requirement 18: Test Isolation and Governance

**User Story:** As a test engineer, I want strict test isolation, so that API tests cannot access internal service components or modify production code.

#### Acceptance Criteria

1. THE Framework SHALL NOT import any packages matching *.domain.* or *.application.*
2. THE Framework SHALL NOT perform database validations or direct database access
3. THE Framework SHALL NOT perform message broker validations or direct Kafka access
4. THE Framework SHALL NOT run gradle, mvn, curl, docker, or test commands during generation
5. THE Framework SHALL confine all generated code to the api-tests module
6. THE Framework SHALL use TestNG exclusively (not JUnit) for test execution


### Requirement 19: Kiro Agent Integration

**User Story:** As a test engineer, I want to use Kiro agents to generate tests, so that I can leverage AI-powered test automation within my IDE.

#### Acceptance Criteria

1. THE Framework SHALL organize prompts in `.kiro/agents/` directory for Kiro integration
2. THE Framework SHALL structure each agent as a separate markdown file with clear instructions
3. THE Framework SHALL include governing documents (TEST_GENERATION_BLUEPRINT.md, 00-agent-operating-rules.md) as context
4. THE Framework SHALL support Claude Sonnet as the AI model via Kiro's backend
5. THE Framework SHALL configure inference parameters: temperature=0.2 for consistent output
6. THE Framework SHALL structure prompts for optimal Claude performance
7. THE Framework SHALL include service type context: JSON REST, XML REST, or SOAP
8. THE Framework SHALL include execution mode context: audit, delta, or full

### Requirement 20: Agent Prompt Structure

**User Story:** As a test engineer, I want well-structured agent prompts, so that I get consistent and high-quality test generation output.

#### Acceptance Criteria

1. THE Framework SHALL include service type in each agent prompt: JSON REST, XML REST, or SOAP
2. THE Framework SHALL include execution mode in each agent prompt: audit, delta, or full
3. THE Framework SHALL include contract source in each agent prompt: snapshot, runtime, code, or wsdl
4. THE Framework SHALL provide explicit output format instructions in each prompt
5. THE Framework SHALL include examples in prompts where appropriate
6. THE Framework SHALL reference governing documents at the start of each agent prompt
