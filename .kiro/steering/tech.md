---
inclusion: always
---

# Technology Stack & Development Guide

## Core Technologies

### Build System
- **Gradle 8.x** multi-module project with Java 17 toolchain
- **Spring Boot 3.3.2** - Use Spring Boot starters for consistency
- Module naming: `services:{service-name}` in settings.gradle

### Required Dependencies Per Service
- `spring-boot-starter-web` - REST APIs
- `spring-boot-starter-validation` - Bean validation
- `spring-boot-starter-actuator` - Health checks
- `spring-boot-starter-data-jpa` - Database access
- `spring-kafka` - Event streaming
- `springdoc-openapi-starter-webmvc-ui` - API docs (auto-generated at `/swagger-ui.html`)

### Database Technologies
- **PostgreSQL 15+**: All services use PostgreSQL
  - Booking service: port 5433
  - Inventory service: port 5434
  - Payment service: port 5435 (currently stateless, no persistence layer)
  - Use Flyway for schema migrations
  - Connection pooling via HikariCP (default)

### Messaging Infrastructure
- **Apache Kafka 3.x** (KRaft mode, no Zookeeper)
  - Bootstrap servers: `localhost:9092` (external), `kafka:29092` (internal)
  - Topic naming: `{service}.{event-type}` (e.g., `booking.events`, `inventory.events`)
  - Use JSON serialization for events
- **Kafka UI**: http://localhost:8085 for topic inspection

## Service Configuration

### Port Assignments (MUST follow)
- Booking Service: **8081**
- Inventory Service: **8082**
- Payment Service: **8083**
- PostgreSQL (Booking): **5433**
- PostgreSQL (Inventory): **5434**
- PostgreSQL (Payment): **5435**
- Kafka: **9092** (external), **29092** (internal)
- Kafka UI: **8085**

### Spring Profiles
- `local`: Development with Docker infrastructure (default)
- `prod`: Production configuration
- Activate via `spring.profiles.active=local` or `SPRING_PROFILES_ACTIVE=local`

## Development Workflow

### Starting Infrastructure
ALWAYS start infrastructure before running services:
```bash
docker-compose -f infra/docker-compose.yml up -d
```

Verify Kafka is ready before starting services (check Kafka UI at http://localhost:8085).

### Building Services
```bash
# Build all services (recommended before running)
./gradlew build

# Build specific service
./gradlew :services:booking-service:build

# Clean build (when dependencies change)
./gradlew clean build

# Skip tests for faster builds
./gradlew build -x test
```

### Running Services
```bash
# Run with Gradle (includes hot reload)
./gradlew :services:booking-service:bootRun
./gradlew :services:inventory-service:bootRun
./gradlew :services:payment-service:bootRun

# Run with specific profile
./gradlew :services:booking-service:bootRun --args='--spring.profiles.active=local'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for specific service
./gradlew :services:booking-service:test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew :services:booking-service:test --tests BookingAppServiceTest
```

### Database Migrations (All Services with Persistence)
- Use Flyway for schema changes
- Migrations in `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__init.sql`, `V2__add_status_index.sql`)
- Version numbers are sequential integers
- NEVER modify existing migrations - create new ones
- Migrations run automatically on application startup
- Current services with migrations:
  - Booking service: V1 (init), V2 (add reservation and payment ids)
  - Inventory service: V1 (init)
  - Payment service: No migrations (stateless)

## Code Generation & Tooling

### Lombok (Inventory Service)
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@Slf4j` for logging
- Use `@Data` sparingly (prefer immutability)
- Ensure Lombok plugin is installed in IDE

### OpenAPI Documentation
- Auto-generated from code annotations
- Access Swagger UI:
  - Booking: http://localhost:8081/swagger-ui.html
  - Inventory: http://localhost:8082/swagger-ui.html
  - Payment: http://localhost:8083/swagger-ui.html
- Use `@Operation`, `@ApiResponse` for better docs
- DTOs automatically generate schemas

## Testing Framework

### JUnit 5 (Jupiter)
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Use `@DataJpaTest` for repository tests
- Use `@EmbeddedKafka` or Testcontainers for Kafka tests

### Test Naming Convention
- Test classes: `{ClassName}Test` or `{ClassName}IntegrationTest`
- Test methods: `should{ExpectedBehavior}_when{Condition}()`
- Example: `shouldCreateBooking_whenValidRequest()`

## Dependency Management Rules

### Adding Dependencies
1. Check if dependency exists in Spring Boot BOM (no version needed)
2. Add to service-specific `build.gradle`, NOT root `build.gradle`
3. Use `implementation` for runtime dependencies
4. Use `testImplementation` for test dependencies
5. Avoid version conflicts by using Spring Boot managed versions

### Common Dependencies
```gradle
// REST API
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-validation'

// Database (for services with persistence)
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'org.postgresql:postgresql'
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'

// Kafka
implementation 'org.springframework.kafka:spring-kafka'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'

// Testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.kafka:spring-kafka-test'
```

## Troubleshooting

### Common Issues
- **Port already in use**: Check if service is already running (`lsof -i :8081`)
- **Kafka connection refused**: Ensure Docker Compose is running
- **Database migration failed**: Check Flyway version table and migration files
- **Tests failing**: Ensure test database is clean (use `@DirtiesContext` if needed)
- **Lombok not working**: Enable annotation processing in IDE

### Debugging
- Enable debug logging: `logging.level.com.letzautomate=DEBUG`
- Check actuator health: `curl http://localhost:8081/actuator/health`
- View Kafka messages in Kafka UI: http://localhost:8085
- Check database state: Connect to PostgreSQL on port 5433

## Performance Considerations

- Use connection pooling (HikariCP configured by default)
- Configure Kafka consumer batch size for throughput
- Use database indexes for frequently queried fields
- Enable Spring Boot caching where appropriate
- Monitor with Spring Boot Actuator metrics
