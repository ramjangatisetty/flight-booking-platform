---
inclusion: always
---

# Project Structure & Architecture Rules

## Multi-Module Gradle Project

This is a Gradle multi-module project with three microservices. When creating new services or modules:
- Add service directory under `services/{service-name}/`
- Register in `settings.gradle` as `include 'services:{service-name}'`
- Create `build.gradle` in service directory with Spring Boot plugin
- Never modify root `build.gradle` for service-specific dependencies

## Hexagonal Architecture (Ports & Adapters)

MUST follow this layered structure for ALL services:

### Layer Responsibilities
- **api/**: REST endpoints, DTOs, validation - NO business logic
- **application/**: Business orchestration, transaction boundaries - coordinates domain and infrastructure
- **domain/**: Core business entities and logic - framework-agnostic
- **infrastructure/**: External integrations (Kafka, JPA, HTTP clients) - adapters to outside world

### Dependency Rules
- api → application → domain ← infrastructure
- Domain MUST NOT depend on infrastructure or api
- Infrastructure implements interfaces defined in application/domain
- Use dependency injection for all cross-layer communication

## Mandatory Package Structure

```
services/{service-name}/src/main/java/com/letzautomate/{service}/
├── {Service}Application.java              # Spring Boot main class
├── api/
│   ├── controller/                        # @RestController classes
│   └── dto/                               # Request/Response records/classes
├── application/
│   └── {Service}AppService.java           # @Service with @Transactional
├── domain/
│   └── model/                             # Plain Java domain objects
└── infrastructure/
    ├── messaging/
    │   ├── config/                        # Kafka @Configuration
    │   ├── consumer/                      # @KafkaListener classes
    │   ├── producer/                      # KafkaTemplate wrappers
    │   └── event/                         # Event POJOs/records
    └── persistence/
        ├── entity/                        # @Entity JPA classes
        └── repository/                    # JpaRepository interfaces
```

## Strict Naming Conventions

### Classes (MUST follow exactly)
- Controllers: `{Entity}Controller` - e.g., `BookingController`, `InventoryQueryController`
- Application Services: `{Entity}AppService` or `{Entity}Service` - e.g., `BookingAppService`
- JPA Entities: `{Entity}Entity` - e.g., `BookingEntity`, `InventoryItemEntity`
- Domain Models: `{Entity}` - e.g., `Booking`, `Reservation`
- Repositories: `{Entity}JpaRepository` or `{Entity}Repository` - e.g., `BookingJpaRepository`
- Event Classes: `{Event}Event` - e.g., `BookingCreatedEvent`, `InventoryReservedEvent`
- DTOs: `{Action}{Entity}Request` or `{Entity}Response` - e.g., `CreateBookingRequest`, `BookingResponse`
- Event Listeners: `{Domain}EventsListener` or `{Domain}Processor` - e.g., `InventoryEventsListener`, `PaymentProcessor`
- Event Publishers: `{Domain}EventPublisher` - e.g., `BookingEventPublisher`

### Event Type Strings
- Format: `{domain}.{action}.{version}` where action is PAST TENSE
- Examples: `booking.created.v1`, `inventory.reserved.v1`, `payment.succeeded.v1`
- MUST wrap in `EventEnvelope<T>` with eventId, eventType, timestamp, correlationId

### Package Names
- Base: `com.letzautomate.{service}` where service is singular (booking, inventory, payment)
- Subpackages: lowercase, no abbreviations (controller not ctrl, repository not repo)

## File Placement Rules

### New REST Endpoints
- Controller class → `api/controller/`
- Request DTOs → `api/dto/`
- Response DTOs → `api/dto/`
- Annotate with `@RestController`, `@RequestMapping`, `@Validated`

### New Business Logic
- Service class → `application/`
- Annotate with `@Service`, `@Transactional` on methods that modify state
- Inject repositories and event publishers via constructor

### New Domain Entities
- Domain model → `domain/model/`
- NO Spring annotations, NO JPA annotations
- Pure Java with business logic methods

### New Database Tables
- JPA entity → `infrastructure/persistence/entity/`
- Repository interface → `infrastructure/persistence/repository/`
- Flyway migration → `src/main/resources/db/migration/V{n}__{description}.sql` (booking-service only)

### New Kafka Events
- Event class → `infrastructure/messaging/event/`
- Consumer → `infrastructure/messaging/consumer/` with `@KafkaListener`
- Producer → `infrastructure/messaging/producer/` with `KafkaTemplate`
- Config → `infrastructure/messaging/config/` with `@Configuration`

## Configuration File Rules

### Profile Strategy
- `application.yml`: Shared config (logging, actuator, common properties)
- `application-local.yml`: Local development (localhost URLs, embedded DBs)
- `application-prod.yml`: Production overrides (external URLs, connection pools)
- Activate with `spring.profiles.active=local` or `prod`

### Required Properties Per Service
```yaml
spring:
  application:
    name: {service-name}-service  # e.g., booking-service
server:
  port: {port}                     # 8081, 8082, 8083
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## Database Migration Rules (Booking Service Only)

- Use Flyway for schema changes
- Migrations in `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__init.sql`, `V2__add_cancellation.sql`)
- Version numbers are sequential integers
- NEVER modify existing migrations - create new ones
- Migrations run automatically on application startup

## Testing Structure

### Test Location
- Unit tests: `src/test/java/{package}/` mirroring main structure
- Integration tests: Same location with `@SpringBootTest`

### Test Naming
- Unit test classes: `{ClassName}Test` - e.g., `BookingAppServiceTest`
- Integration test classes: `{ClassName}IntegrationTest` - e.g., `BookingControllerIntegrationTest`
- Test methods: `should{ExpectedBehavior}_when{Condition}` - e.g., `shouldCreateBooking_whenValidRequest()`

### Test Dependencies
- JUnit 5 (Jupiter) for all tests
- `@SpringBootTest` for integration tests
- `spring-kafka-test` for Kafka integration tests
- Mockito for mocking (prefer constructor injection for testability)

## Code Style Rules

### Dependency Injection
- ALWAYS use constructor injection (not field injection)
- Make fields `private final`
- Let Lombok `@RequiredArgsConstructor` generate constructor or write explicitly

### Immutability
- Prefer Java records for DTOs and events
- Use `@Value` or records for value objects
- Make domain entities immutable where possible

### Annotations
- Controllers: `@RestController`, `@RequestMapping("/api/{resource}")`
- Services: `@Service`, `@Transactional` on state-changing methods
- Repositories: `@Repository` (optional with Spring Data JPA)
- Configuration: `@Configuration`, `@EnableKafka` where needed
- Validation: `@Valid` on controller parameters, `@NotNull`, `@NotBlank` on DTO fields

### Error Handling
- Use `@ControllerAdvice` for global exception handling
- Return appropriate HTTP status codes (400, 404, 409, 500)
- Include correlationId in error logs

## When Adding New Features

1. Identify which service owns the feature (booking, inventory, or payment)
2. Create domain model in `domain/model/` if new entity
3. Create JPA entity in `infrastructure/persistence/entity/`
4. Create repository in `infrastructure/persistence/repository/`
5. Create application service in `application/`
6. Create DTOs in `api/dto/`
7. Create controller in `api/controller/`
8. Add Flyway migration if schema changes (booking-service only)
9. Create events in `infrastructure/messaging/event/` if cross-service communication needed
10. Add event publisher/consumer in `infrastructure/messaging/`

## Anti-Patterns to Avoid

- DO NOT put business logic in controllers
- DO NOT use field injection (`@Autowired` on fields)
- DO NOT create circular dependencies between services
- DO NOT access repositories directly from controllers
- DO NOT put JPA annotations on domain models
- DO NOT create god classes - keep services focused
- DO NOT skip transaction boundaries on state-changing operations
- DO NOT forget correlationId propagation in events
