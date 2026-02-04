# Prompt 02 — Create api-tests Module (No Production Code Changes)

Follow `TEST_GENERATION_BLUEPRINT.md`.

Task:
1) Create a new Gradle module named `api-tests` at repo root and wire it into settings.
2) Add dependencies (test scope) in api-tests:
    - RestAssured (for JSON and XML REST)
    - Jackson databind (+ jsr310 for dates)
    - Jackson dataformat-xml (for XML REST services like baggage-service)
    - TestNG (runner)
    - AssertJ (assertions)
    - (optional) slf4j-simple for basic logging
3) Configure Gradle test task to use TestNG:
    - `tasks.test { useTestNG() }`
4) Create initial skeleton packages under:
    - api-tests/src/test/java/framework/...
5) Create the contract snapshot folder conventions:
    - OpenAPI snapshots for REST services:
      - `api-tests/src/test/resources/openapi-snapshots/booking-service/`
      - `api-tests/src/test/resources/openapi-snapshots/inventory-service/`
      - `api-tests/src/test/resources/openapi-snapshots/payment-service/`
      - `api-tests/src/test/resources/openapi-snapshots/baggage-service/`
    - WSDL/XSD snapshots for SOAP services:
      - `api-tests/src/test/resources/wsdl-snapshots/loyalty-service/`
6) Add smoke tests:
    - JSON REST smoke test: GET `/api-docs` for booking-service
    - XML REST smoke test: GET `/api-docs` for baggage-service
    - SOAP smoke test: Verify WSDL accessible at `/ws?wsdl` for loyalty-service

## Dependencies (build.gradle.kts)

```kotlin
plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    // REST client
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    
    // JSON serialization
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    
    // XML serialization (for baggage-service XML REST)
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.2")
    
    // Test framework
    testImplementation("org.testng:testng:7.10.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
    
    // Logging
    testImplementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useTestNG()
}
```

## Environment Variables

The module should read these environment variables:
- `BASE_URL_BOOKING` - Booking service base URL (default: http://localhost:8081)
- `BASE_URL_INVENTORY` - Inventory service base URL (default: http://localhost:8082)
- `BASE_URL_PAYMENT` - Payment service base URL (default: http://localhost:8083)
- `BASE_URL_BAGGAGE` - Baggage service base URL (default: http://localhost:8084)
- `BASE_URL_LOYALTY` - Loyalty service base URL (default: http://localhost:8085)
- `ENV` - Environment name (local/dev/qa)
- `LOG_HTTP` - Enable HTTP logging (true/false)

## Constraints
- Do NOT modify any production code in service modules.
- Do NOT import domain/application packages.
- Output diffs for created/modified files only.
