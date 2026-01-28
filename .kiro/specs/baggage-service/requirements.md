# Requirements Document: Baggage Service

## Introduction

The Baggage Service is a new microservice for the flight booking platform that manages baggage check-in and tracking operations. Unlike other services in the platform, this service exclusively uses XML for all API interactions to simulate legacy airline baggage systems that rely on XML payloads and schema-driven contracts.

## Glossary

- **Baggage_Service**: The microservice responsible for baggage check-in and tracking operations
- **Bag_Tag**: A unique identifier for checked baggage following the pattern [A-Z]{2}[0-9]{8}
- **XSD**: XML Schema Definition that defines the structure and validation rules for XML documents
- **Airport_Code**: A three-letter IATA airport code following the pattern [A-Z]{3}
- **Baggage_Status**: The current state of a bag in the system (CHECKED_IN, LOADED, IN_TRANSIT, ARRIVED, DELIVERED, LOST)
- **Bag_Event**: A timestamped record of a baggage status change at a specific location
- **Event_Envelope**: A wrapper structure for all Kafka events containing metadata (eventId, eventType, timestamp, correlationId) and payload
- **Correlation_Id**: A unique identifier that traces a request across multiple services in the distributed system
- **Auto_Creation**: The process of automatically creating baggage when a booking is confirmed, without manual intervention
- **Idempotency**: The property that ensures processing the same event multiple times produces the same result as processing it once

## Requirements

### Requirement 1: XML-Only API Contract

**User Story:** As an airline system integrator, I want all API endpoints to exclusively use XML format, so that the baggage service integrates with legacy airline systems that require XML-based communication.

#### Acceptance Criteria

1. WHEN a client sends a request with Content-Type other than application/xml, THEN THE Baggage_Service SHALL return HTTP 415 Unsupported Media Type
2. WHEN a client sends a request with Accept header other than application/xml, THEN THE Baggage_Service SHALL return HTTP 406 Not Acceptable
3. THE Baggage_Service SHALL configure all REST endpoints with consumes="application/xml" and produces="application/xml"
4. WHEN the service returns an error response, THEN THE Baggage_Service SHALL format the error as XML with code, message, and optional bagTag fields

### Requirement 2: XSD Schema Definition

**User Story:** As a system architect, I want XML contracts defined by XSD schemas, so that API consumers can validate requests and responses against a formal specification.

#### Acceptance Criteria

1. THE Baggage_Service SHALL define XSD schemas in src/main/resources/xsd/ directory
2. THE XSD SHALL define BaggageCheckinRequest with fields: bookingId, passengerId, bagTag, origin, destination
3. THE XSD SHALL define BaggageCheckinResponse with fields: bagTag, status, acceptedAt
4. THE XSD SHALL define BaggageTrackResponse with fields: bagTag, status, events array
5. THE XSD SHALL define Error type with fields: code, message, optional bagTag
6. THE XSD SHALL enforce bagTag pattern constraint: [A-Z]{2}[0-9]{8}
7. THE XSD SHALL enforce status enumeration: CHECKED_IN, LOADED, IN_TRANSIT, ARRIVED, DELIVERED, LOST
8. THE XSD SHALL enforce airport code pattern constraint: [A-Z]{3}

### Requirement 3: Runtime XSD Validation

**User Story:** As a service developer, I want incoming XML requests validated against XSD schemas at runtime, so that invalid data is rejected before processing.

#### Acceptance Criteria

1. WHEN a client sends an XML request, THEN THE Baggage_Service SHALL validate the request against the XSD schema
2. WHEN XSD validation fails, THEN THE Baggage_Service SHALL return HTTP 400 Bad Request
3. WHEN XSD validation fails, THEN THE Baggage_Service SHALL return an XML Error response with code="SCHEMA_VALIDATION_FAILED"
4. WHEN XSD validation fails, THEN THE Baggage_Service SHALL include a readable validation error message in the Error response
5. WHEN XSD validation succeeds, THEN THE Baggage_Service SHALL proceed with request processing

### Requirement 4: Baggage Check-In

**User Story:** As a baggage handler, I want to check in passenger baggage, so that bags are registered in the tracking system at the origin airport.

#### Acceptance Criteria

1. THE Baggage_Service SHALL expose POST /baggage/checkin endpoint
2. WHEN a valid BaggageCheckinRequest is received, THEN THE Baggage_Service SHALL create a new bag record with status CHECKED_IN
3. WHEN a valid BaggageCheckinRequest is received, THEN THE Baggage_Service SHALL create an initial bag event with type CHECKED_IN at the origin airport
4. WHEN a check-in request contains a bagTag that already exists, THEN THE Baggage_Service SHALL return the current bag state without creating duplicates (idempotent operation)
5. WHEN baggage is successfully checked in, THEN THE Baggage_Service SHALL return BaggageCheckinResponse with HTTP 200 OK
6. WHEN baggage is successfully checked in, THEN THE Baggage_Service SHALL set acceptedAt to the current server timestamp

### Requirement 5: Baggage Tracking

**User Story:** As a passenger, I want to track my checked baggage, so that I can see its current status and location history.

#### Acceptance Criteria

1. THE Baggage_Service SHALL expose GET /baggage/track/{bagTag} endpoint
2. WHEN a valid bagTag is provided, THEN THE Baggage_Service SHALL return BaggageTrackResponse with current status and all events
3. WHEN a bagTag does not exist in the system, THEN THE Baggage_Service SHALL return HTTP 404 Not Found
4. WHEN a bagTag does not exist, THEN THE Baggage_Service SHALL return an XML Error response with code="BAG_NOT_FOUND"
5. WHEN a bagTag does not exist, THEN THE Baggage_Service SHALL include the requested bagTag in the Error response
6. WHEN returning bag events, THEN THE Baggage_Service SHALL order events chronologically by occurred_at timestamp

### Requirement 6: Administrative Seed Data

**User Story:** As a developer, I want to seed test data for demonstrations, so that I can quickly verify the service functionality without manual data entry.

#### Acceptance Criteria

1. THE Baggage_Service SHALL expose POST /baggage/admin/seed endpoint
2. WHEN the seed endpoint is called, THEN THE Baggage_Service SHALL create one valid bag with a properly formatted bagTag
3. WHEN the seed endpoint is called, THEN THE Baggage_Service SHALL create multiple bag events (CHECKED_IN, LOADED, IN_TRANSIT) for the seeded bag
4. WHEN seeding completes, THEN THE Baggage_Service SHALL return an XML response confirming the seeded bagTag
5. WHEN the seed endpoint is called multiple times, THEN THE Baggage_Service SHALL handle idempotency appropriately

### Requirement 7: Data Persistence

**User Story:** As a system administrator, I want baggage data persisted in PostgreSQL, so that tracking information survives service restarts and can be queried reliably.

#### Acceptance Criteria

1. THE Baggage_Service SHALL use PostgreSQL database on port 5437 with database name baggagedb
2. THE Baggage_Service SHALL use Flyway for database schema migrations
3. THE Baggage_Service SHALL create a bags table with columns: bag_tag (PK), booking_id, passenger_id, origin, destination, status, created_at
4. THE Baggage_Service SHALL create a bag_events table with columns: id (PK), bag_tag (FK), event_type, airport, occurred_at
5. THE Baggage_Service SHALL create an index on bag_events(bag_tag) for efficient event queries
6. WHEN storing bag events, THEN THE Baggage_Service SHALL preserve chronological order by occurred_at timestamp
7. THE Baggage_Service SHALL enforce referential integrity between bag_events and bags tables

### Requirement 8: Service Configuration

**User Story:** As a DevOps engineer, I want the service properly configured for local development, so that it integrates seamlessly with the existing platform infrastructure.

#### Acceptance Criteria

1. THE Baggage_Service SHALL run on port 8085
2. THE Baggage_Service SHALL be configured as a Gradle module under services/baggage-service
3. THE Baggage_Service SHALL use Java 17 and Spring Boot 3.3.2
4. THE Baggage_Service SHALL include application-local.yml with JDBC URL jdbc:postgresql://localhost:5437/baggagedb
5. THE Baggage_Service SHALL configure database credentials: username=baggage, password=baggage
6. THE Baggage_Service SHALL include Spring Boot Actuator with health endpoint exposed
7. THE Baggage_Service SHALL follow the hexagonal architecture pattern with api/application/domain/infrastructure layers
8. WHEN built with ./gradlew build, THEN THE Baggage_Service SHALL compile successfully and pass all tests

### Requirement 9: Docker Infrastructure

**User Story:** As a developer, I want the baggage database containerized, so that I can start the complete development environment with docker-compose.

#### Acceptance Criteria

1. THE infrastructure configuration SHALL include a baggage-db PostgreSQL container
2. THE baggage-db container SHALL expose port 5437 on the host
3. THE baggage-db container SHALL use database name baggagedb
4. THE baggage-db container SHALL use credentials: user=baggage, password=baggage
5. WHEN docker-compose up is executed, THEN THE baggage-db container SHALL start successfully
6. WHEN the Baggage_Service starts with local profile, THEN THE Baggage_Service SHALL connect to the baggage-db container successfully

### Requirement 10: Error Handling

**User Story:** As an API consumer, I want consistent XML error responses, so that I can programmatically handle errors in a predictable way.

#### Acceptance Criteria

1. THE Baggage_Service SHALL implement a global exception handler using @ControllerAdvice
2. WHEN any error occurs, THEN THE Baggage_Service SHALL return an XML Error response
3. THE Error response SHALL include a code field with a machine-readable error code
4. THE Error response SHALL include a message field with a human-readable description
5. WHEN an error is related to a specific bagTag, THEN THE Error response SHALL include the bagTag field
6. THE Baggage_Service SHALL use appropriate HTTP status codes: 400 for validation errors, 404 for not found, 415 for unsupported media type, 406 for not acceptable, 500 for server errors

### Requirement 11: Event-Driven Integration with Booking Service

**User Story:** As a passenger, I want baggage automatically created when my booking is confirmed, so that I don't need to manually check in baggage separately.

#### Acceptance Criteria

1. THE Baggage_Service SHALL consume booking.confirmed.v1 events from the booking.events Kafka topic
2. WHEN a booking.confirmed.v1 event is received, THEN THE Baggage_Service SHALL automatically create baggage for that booking
3. WHEN auto-creating baggage, THEN THE Baggage_Service SHALL generate a unique bagTag following the pattern [Airline Code][8 Random Digits]
4. WHEN auto-creating baggage, THEN THE Baggage_Service SHALL extract the airline code from the flightId (first 2 characters)
5. WHEN auto-creating baggage, THEN THE Baggage_Service SHALL derive origin and destination airports from the flightId
6. WHEN baggage is auto-created, THEN THE Baggage_Service SHALL set initial status to CHECKED_IN
7. WHEN baggage is auto-created, THEN THE Baggage_Service SHALL create an initial bag event with type CHECKED_IN at the origin airport
8. WHEN baggage auto-creation fails, THEN THE Baggage_Service SHALL log the error but NOT fail the booking confirmation
9. THE Baggage_Service SHALL prevent duplicate baggage creation for the same bookingId (idempotency)
10. WHEN checking for duplicate baggage, THEN THE Baggage_Service SHALL query by bookingId before creating new baggage

### Requirement 12: Baggage Event Publishing

**User Story:** As a booking service, I want to be notified when baggage is checked in, so that I can update the booking record with the bagTag.

#### Acceptance Criteria

1. THE Baggage_Service SHALL publish baggage.checked_in.v1 events to the baggage.events Kafka topic
2. WHEN baggage is successfully created, THEN THE Baggage_Service SHALL publish a baggage.checked_in.v1 event
3. THE baggage.checked_in.v1 event SHALL include: bookingId, passengerId, bagTag, origin, destination, status, checkedInAt
4. THE Baggage_Service SHALL publish baggage.status_updated.v1 events to the baggage.events Kafka topic
5. WHEN baggage status is updated, THEN THE Baggage_Service SHALL publish a baggage.status_updated.v1 event
6. THE baggage.status_updated.v1 event SHALL include: bookingId, bagTag, previousStatus, newStatus, airport, updatedAt
7. ALL published events SHALL be wrapped in EventEnvelope with: eventId, eventType, eventVersion, occurredAt, correlationId, producer
8. WHEN publishing events, THEN THE Baggage_Service SHALL propagate the correlationId from the incoming booking event
9. WHEN publishing events, THEN THE Baggage_Service SHALL generate a unique eventId (UUID) for each event
10. WHEN publishing events, THEN THE Baggage_Service SHALL set producer field to "baggage-service"

### Requirement 13: Baggage Status Update API

**User Story:** As a baggage handler, I want to update baggage status as it moves through the airport, so that passengers can track their bags in real-time.

#### Acceptance Criteria

1. THE Baggage_Service SHALL expose PUT /baggage/status/{bagTag} endpoint
2. THE endpoint SHALL accept BaggageStatusUpdateRequest with fields: status, airport
3. WHEN a valid status update request is received, THEN THE Baggage_Service SHALL update the bag status
4. WHEN a valid status update request is received, THEN THE Baggage_Service SHALL create a new bag event with the updated status
5. WHEN status is updated, THEN THE Baggage_Service SHALL publish a baggage.status_updated.v1 event
6. WHEN the bagTag does not exist, THEN THE Baggage_Service SHALL return HTTP 404 Not Found
7. WHEN status update succeeds, THEN THE Baggage_Service SHALL return BaggageStatusUpdateResponse with HTTP 200 OK
8. THE BaggageStatusUpdateResponse SHALL include: bagTag, previousStatus, newStatus, updatedAt
9. WHEN status is updated, THEN THE Baggage_Service SHALL preserve the previous status in the response
10. THE endpoint SHALL validate status against the allowed enumeration values
