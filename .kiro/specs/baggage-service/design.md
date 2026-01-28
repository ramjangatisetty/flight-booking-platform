# Design Document: Baggage Service

## 1. System Overview

The Baggage Service is an XML-only microservice that manages airline baggage check-in and tracking operations. It integrates with the booking service through an event-driven architecture, automatically creating baggage when bookings are confirmed.

### 1.1 Architecture Style

- **Hexagonal Architecture (Ports & Adapters)**: Clean separation between business logic and external integrations
- **Event-Driven Integration**: Asynchronous communication via Kafka for loose coupling
- **XML-First API**: All REST endpoints exclusively use XML for requests and responses
- **Schema-Driven Contracts**: XSD schemas define and validate all XML payloads

### 1.2 Technology Stack

- **Runtime**: Java 17, Spring Boot 3.3.2
- **Database**: PostgreSQL 16 (port 5437)
- **Messaging**: Apache Kafka 3.x
- **XML Processing**: Jackson XML, JAXB
- **Schema Validation**: XSD with Bean Validation
- **Migrations**: Flyway
- **Build**: Gradle 8.x

## 2. Domain Model

### 2.1 Core Entities

#### Bag
```java
class Bag {
    String bagTag;           // PK, format: [A-Z]{2}[0-9]{8}
    String bookingId;        // FK to booking service
    String passengerId;      // FK to passenger (future)
    String origin;           // IATA airport code [A-Z]{3}
    String destination;      // IATA airport code [A-Z]{3}
    BaggageStatus status;    // Current status
    Instant createdAt;       // Creation timestamp
}
```

#### BagEvent
```java
class BagEvent {
    Long id;                 // PK
    String bagTag;           // FK to Bag
    String eventType;        // Status value
    String airport;          // IATA airport code
    Instant occurredAt;      // Event timestamp
}
```

#### BaggageStatus (Enum)
```
CHECKED_IN    → Initial state when bag is accepted
LOADED        → Bag loaded onto aircraft
IN_TRANSIT    → Bag in transit between airports
ARRIVED       → Bag arrived at destination
DELIVERED     → Bag delivered to passenger
LOST          → Bag cannot be located
```

### 2.2 State Transitions

```
CHECKED_IN → LOADED → IN_TRANSIT → ARRIVED → DELIVERED
                ↓
              LOST (from any state)
```

## 3. API Design

### 3.1 REST Endpoints

All endpoints consume and produce `application/xml` exclusively.

#### POST /baggage/checkin
**Purpose**: Manually check in baggage  
**Request**: BaggageCheckinRequest  
**Response**: BaggageCheckinResponse (200 OK)  
**Errors**: 400 (validation), 415 (wrong content type)

#### GET /baggage/track/{bagTag}
**Purpose**: Track baggage and view event history  
**Response**: BaggageTrackResponse (200 OK)  
**Errors**: 404 (bag not found), 406 (wrong accept header)

#### PUT /baggage/status/{bagTag}
**Purpose**: Update baggage status  
**Request**: BaggageStatusUpdateRequest  
**Response**: BaggageStatusUpdateResponse (200 OK)  
**Errors**: 400 (validation), 404 (bag not found), 415 (wrong content type)

#### POST /baggage/admin/seed
**Purpose**: Seed demo data for testing  
**Response**: SeedResponse (200 OK)

### 3.2 XML Schemas (XSD)

#### BaggageCheckinRequest
```xml
<xs:complexType name="BaggageCheckinRequest">
  <xs:sequence>
    <xs:element name="bookingId" type="xs:string"/>
    <xs:element name="passengerId" type="xs:string"/>
    <xs:element name="bagTag" type="BagTagType"/>
    <xs:element name="origin" type="AirportCodeType"/>
    <xs:element name="destination" type="AirportCodeType"/>
  </xs:sequence>
</xs:complexType>

<xs:simpleType name="BagTagType">
  <xs:restriction base="xs:string">
    <xs:pattern value="[A-Z]{2}[0-9]{8}"/>
  </xs:restriction>
</xs:simpleType>

<xs:simpleType name="AirportCodeType">
  <xs:restriction base="xs:string">
    <xs:pattern value="[A-Z]{3}"/>
  </xs:restriction>
</xs:simpleType>
```

#### BaggageStatusUpdateRequest
```xml
<xs:complexType name="BaggageStatusUpdateRequest">
  <xs:sequence>
    <xs:element name="status" type="BaggageStatusType"/>
    <xs:element name="airport" type="AirportCodeType"/>
  </xs:sequence>
</xs:complexType>

<xs:simpleType name="BaggageStatusType">
  <xs:restriction base="xs:string">
    <xs:enumeration value="CHECKED_IN"/>
    <xs:enumeration value="LOADED"/>
    <xs:enumeration value="IN_TRANSIT"/>
    <xs:enumeration value="ARRIVED"/>
    <xs:enumeration value="DELIVERED"/>
    <xs:enumeration value="LOST"/>
  </xs:restriction>
</xs:simpleType>
```

## 4. Event-Driven Integration

### 4.1 Event Flow Architecture

```
┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│  Booking Service │         │      Kafka       │         │ Baggage Service  │
└──────────────────┘         └──────────────────┘         └──────────────────┘
         │                            │                            │
         │  1. Booking confirmed      │                            │
         ├───────────────────────────>│                            │
         │  booking.confirmed.v1      │                            │
         │                            │  2. Consume event          │
         │                            ├───────────────────────────>│
         │                            │                            │
         │                            │                   3. Auto-create baggage
         │                            │                            │
         │                            │  4. Publish event          │
         │                            │<───────────────────────────┤
         │                            │  baggage.checked_in.v1     │
         │  5. Consume event          │                            │
         │<───────────────────────────┤                            │
         │                            │                            │
    6. Update booking                 │                            │
    with bagTag                       │                            │
```

### 4.2 Event Definitions

#### EventEnvelope Structure
All events use a standardized envelope:

```json
{
  "meta": {
    "eventId": "uuid",           // Unique event identifier
    "eventType": "string",       // Event type (e.g., "booking.confirmed.v1")
    "eventVersion": 1,           // Schema version
    "occurredAt": "timestamp",   // ISO-8601 timestamp
    "correlationId": "uuid",     // Trace ID across services
    "producer": "string"         // Source service name
  },
  "data": {
    // Event-specific payload
  }
}
```

#### booking.confirmed.v1 (Consumed)
**Topic**: `booking.events`  
**Consumer Group**: `baggage-service-v2`

**Payload**:
```json
{
  "bookingId": "uuid",
  "reservationId": "uuid",
  "paymentId": "uuid",
  "flightId": "string",        // e.g., "AA123"
  "seatClass": "string",
  "amount": "decimal",
  "currency": "string",
  "status": "CONFIRMED",
  "passengerId": "uuid",
  "origin": "string",          // May be null
  "destination": "string"      // May be null
}
```

#### baggage.checked_in.v1 (Published)
**Topic**: `baggage.events`

**Payload**:
```json
{
  "bookingId": "uuid",
  "passengerId": "uuid",
  "bagTag": "string",          // e.g., "AA12345678"
  "origin": "string",          // IATA code
  "destination": "string",     // IATA code
  "status": "CHECKED_IN",
  "checkedInAt": "timestamp"
}
```

#### baggage.status_updated.v1 (Published)
**Topic**: `baggage.events`

**Payload**:
```json
{
  "bookingId": "uuid",
  "bagTag": "string",
  "previousStatus": "string",
  "newStatus": "string",
  "airport": "string",
  "updatedAt": "timestamp"
}
```

### 4.3 Event Processing Rules

#### Idempotency
- Check for existing baggage by `bookingId` before creating
- If baggage exists, skip creation and log warning
- Use `eventId` for deduplication (future enhancement)

#### Error Handling
- Baggage creation failures do NOT fail booking confirmation
- Log errors with correlationId for debugging
- Consider dead letter queue for failed events (future)

#### Correlation Tracking
- Propagate `correlationId` from booking.confirmed.v1 to baggage.checked_in.v1
- Include correlationId in all log statements
- Use correlationId for distributed tracing

## 5. Business Logic

### 5.1 BagTag Generation

**Algorithm**:
```java
String generateBagTag(String flightId) {
    // Extract airline code (first 2 chars of flightId)
    String airlineCode = flightId.substring(0, 2).toUpperCase();
    
    // Generate 8 random digits
    int randomNumber = 10000000 + RANDOM.nextInt(90000000);
    
    return airlineCode + randomNumber;
}
```

**Examples**:
- Flight AA123 → AA12345678
- Flight UA456 → UA98765432
- Flight DL789 → DL55667788

### 5.2 Route Derivation

Since booking service doesn't store origin/destination, derive from flightId:

**Mapping** (Demo Logic):
```java
Map<String, Route> AIRLINE_ROUTES = Map.of(
    "AA", new Route("DFW", "LAX"),  // American Airlines
    "UA", new Route("ORD", "SFO"),  // United Airlines
    "DL", new Route("ATL", "JFK")   // Delta Airlines
);

Route deriveRoute(String flightId) {
    String airlineCode = flightId.substring(0, 2);
    return AIRLINE_ROUTES.getOrDefault(airlineCode, 
        new Route("JFK", "LAX"));  // Default route
}
```

**Production Enhancement**: Query a flight service for actual route data.

### 5.3 Auto-Creation Workflow

```java
@Transactional
public String createBaggageForBooking(
    String bookingId,
    String passengerId,
    String flightId,
    String correlationId
) {
    // 1. Check for existing baggage (idempotency)
    Optional<BagEntity> existing = bagRepository.findByBookingId(bookingId);
    if (existing.isPresent()) {
        log.warn("Baggage already exists for booking: {}", bookingId);
        return existing.get().getBagTag();
    }
    
    // 2. Generate unique bagTag
    String bagTag = generateBagTag(flightId);
    
    // 3. Derive route from flightId
    Route route = deriveRoute(flightId);
    
    // 4. Create bag entity
    BagEntity bag = new BagEntity();
    bag.setBagTag(bagTag);
    bag.setBookingId(bookingId);
    bag.setPassengerId(passengerId);
    bag.setOrigin(route.origin());
    bag.setDestination(route.destination());
    bag.setStatus("CHECKED_IN");
    bag.setCreatedAt(Instant.now());
    bagRepository.save(bag);
    
    // 5. Create initial event
    BagEventEntity event = new BagEventEntity();
    event.setBagTag(bagTag);
    event.setEventType("CHECKED_IN");
    event.setAirport(route.origin());
    event.setOccurredAt(Instant.now());
    bagEventRepository.save(event);
    
    // 6. Publish baggage.checked_in.v1 event
    baggageEventPublisher.publishBaggageCheckedIn(
        bookingId, passengerId, bagTag, 
        route.origin(), route.destination(), correlationId
    );
    
    return bagTag;
}
```

## 6. Data Persistence

### 6.1 Database Schema

#### bags table
```sql
CREATE TABLE bags (
    bag_tag VARCHAR(10) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL UNIQUE,
    passenger_id VARCHAR(36) NOT NULL,
    origin VARCHAR(3) NOT NULL,
    destination VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bags_booking_id ON bags(booking_id);
```

#### bag_events table
```sql
CREATE TABLE bag_events (
    id BIGSERIAL PRIMARY KEY,
    bag_tag VARCHAR(10) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    airport VARCHAR(3) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bag_tag) REFERENCES bags(bag_tag) ON DELETE CASCADE
);

CREATE INDEX idx_bag_events_bag_tag ON bag_events(bag_tag);
CREATE INDEX idx_bag_events_occurred_at ON bag_events(occurred_at);
```

### 6.2 Repository Interfaces

```java
public interface BagRepository extends JpaRepository<BagEntity, String> {
    Optional<BagEntity> findByBookingId(String bookingId);
}

public interface BagEventRepository extends JpaRepository<BagEventEntity, Long> {
    List<BagEventEntity> findByBagTagOrderByOccurredAtAsc(String bagTag);
}
```

## 7. Integration Points

### 7.1 Kafka Configuration

**Bootstrap Servers**: `localhost:9092` (local), `kafka:29092` (docker)

**Topics**:
- `booking.events` - Consumed by baggage service
- `baggage.events` - Published by baggage service

**Consumer Configuration**:
```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: baggage-service-v2
      auto-offset-reset: earliest
      key-deserializer: StringDeserializer
      value-deserializer: JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
```

**Producer Configuration**:
```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
```

### 7.2 Booking Service Integration

The booking service:
1. Publishes `booking.confirmed.v1` when booking reaches CONFIRMED status
2. Consumes `baggage.checked_in.v1` to update booking with bagTag
3. Stores bagTag in `bookings.bag_tag` column (added via migration V4)
4. Returns bagTag in BookingResponse DTO

**Database Migration** (Booking Service):
```sql
ALTER TABLE bookings ADD COLUMN bag_tag VARCHAR(10);
CREATE INDEX idx_bookings_bag_tag ON bookings(bag_tag);
```

## 8. Error Handling

### 8.1 Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BagNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBagNotFound(BagNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("BAG_NOT_FOUND", ex.getMessage(), ex.getBagTag()));
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", 
                "Only application/xml is supported", null));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_FAILED", 
                ex.getBindingResult().toString(), null));
    }
}
```

### 8.2 Error Response Format

```xml
<ErrorResponse>
  <code>BAG_NOT_FOUND</code>
  <message>Bag not found: AA12345678</message>
  <bagTag>AA12345678</bagTag>
</ErrorResponse>
```

## 9. Testing Strategy

### 9.1 Unit Tests
- BagTag generation logic
- Route derivation logic
- Status transition validation
- XML serialization/deserialization

### 9.2 Integration Tests
- REST endpoint tests with XML payloads
- XSD validation tests
- Database persistence tests
- Kafka event publishing/consuming tests

### 9.3 End-to-End Tests
- Complete booking → baggage flow
- Status update → event publishing
- Idempotency verification
- Error scenario handling

## 10. Deployment Configuration

### 10.1 Service Configuration

**Port**: 8085  
**Database**: PostgreSQL on port 5437  
**Kafka**: localhost:9092

### 10.2 Docker Compose

```yaml
baggage-db:
  image: postgres:16
  container_name: baggage-db
  environment:
    POSTGRES_DB: baggagedb
    POSTGRES_USER: baggage
    POSTGRES_PASSWORD: baggage
  ports:
    - "5437:5432"
  volumes:
    - baggage-db-data:/var/lib/postgresql/data
```

### 10.3 Application Properties

```yaml
server:
  port: 8085

spring:
  application:
    name: baggage-service
  datasource:
    url: jdbc:postgresql://localhost:5437/baggagedb
    username: baggage
    password: baggage
  kafka:
    bootstrap-servers: localhost:9092
```

## 11. Monitoring and Observability

### 11.1 Logging
- Log all event consumption with correlationId
- Log baggage creation/updates
- Log errors with full context
- Use structured logging (JSON format in production)

### 11.2 Metrics
- Baggage creation rate
- Event processing latency
- API response times
- Error rates by endpoint

### 11.3 Health Checks
- Spring Boot Actuator `/actuator/health`
- Database connectivity check
- Kafka connectivity check

## 12. Future Enhancements

### 12.1 Real-time Tracking
- WebSocket support for live status updates
- Push notifications to mobile apps
- Integration with airport baggage systems

### 12.2 Advanced Features
- Multi-bag support per booking
- Baggage weight and dimensions tracking
- Special handling requirements (fragile, oversized)
- Baggage claim carousel assignment

### 12.3 Integration Improvements
- Flight service integration for actual routes
- Passenger service integration for passenger details
- Notification service for baggage alerts
- Analytics service for baggage metrics

## 13. Security Considerations

### 13.1 Authentication
- Future: OAuth2/JWT for API authentication
- Current: Internal service-to-service communication

### 13.2 Authorization
- Future: Role-based access control
- Baggage handlers: Update status only
- Passengers: Read-only tracking

### 13.3 Data Privacy
- PII handling for passenger information
- GDPR compliance for data retention
- Audit logging for sensitive operations
