# Prompt 08 — Local-only Testkit and Resilience Demonstration

Follow `TEST_GENERATION_BLUEPRINT.md`.
Follow `00-agent-operating-rules.md`.

## Overview

This prompt generates tests for local-only testkit endpoints that are used for:
- Resetting service state between tests
- Injecting failures for resilience testing
- Querying events for saga verification

These tests MUST be guarded by `ENV=local` check.

## Task

1) Implement `framework/testkit/LocalTestApi` methods for:
    - POST /test/reset
    - POST /test/failures
    - GET /test/events

2) Create test utilities:
    - LocalTestGuard - throws exception if ENV != local
    - FailureConfig - builder for failure injection configuration

3) Add tests guarded by ENV=local:
    - Reset state before test suite
    - Enable injected failures/latency for one service
    - Demonstrate retry behavior if `framework/resilience` exists
    - Validate ErrorResponse format under fault conditions
    - Disable failures after test

## Generate tests in `api-tests/src/test/java/tests/testkit/LocalTestkitTests`:

### Prerequisites
- All tests MUST check `ENV == local` before execution
- Tests should be skipped (not failed) in non-local environments

### Reset Tests
- POST /test/reset to booking-service
    - Verify 200 response
    - Verify state is cleared

- POST /test/reset to inventory-service
    - Verify 200 response
    - Verify state is cleared

### Failure Injection Tests
- POST /test/failures to enable failures
    - Configure specific failure type (e.g., timeout, error)
    - Verify 200 response

- Make request to service with failures enabled
    - Verify appropriate error response
    - Validate ErrorResponse contract

- POST /test/failures to disable failures
    - Verify 200 response
    - Verify service returns to normal operation

### Event Query Tests (if implemented)
- GET /test/events
    - Verify 200 response
    - Verify response is array of events
    - Verify event structure contains: eventId, eventType, timestamp, correlationId

## LocalTestApi Implementation

```java
public class LocalTestApi {
    private final ApiClient client;
    
    public LocalTestApi(ApiClient client) {
        LocalTestGuard.ensureLocal();
        this.client = client;
    }
    
    public void reset() {
        Response response = client.post("/test/reset", Map.of(), null);
        assertThat(response.statusCode()).isEqualTo(200);
    }
    
    public void configureFailures(FailureConfig config) {
        Response response = client.post("/test/failures", Map.of(), config.toMap());
        assertThat(response.statusCode()).isEqualTo(200);
    }
    
    public List<Map<String, Object>> events() {
        Response response = client.get("/test/events", Map.of());
        assertThat(response.statusCode()).isEqualTo(200);
        return response.jsonPath().getList("$");
    }
}
```

## FailureConfig Builder

```java
public class FailureConfig {
    private boolean enabled;
    private String failureType; // "timeout", "error", "latency"
    private int latencyMs;
    private int errorCode;
    
    public static FailureConfig timeout(int ms) { ... }
    public static FailureConfig error(int statusCode) { ... }
    public static FailureConfig latency(int ms) { ... }
    public static FailureConfig disabled() { ... }
    
    public Map<String, Object> toMap() { ... }
}
```

## LocalTestGuard

```java
public class LocalTestGuard {
    public static void ensureLocal() {
        String env = EnvUtils.getEnv("ENV", "local");
        if (!"local".equalsIgnoreCase(env)) {
            throw new IllegalStateException(
                "Testkit endpoints are only available in local environment. " +
                "Current ENV: " + env
            );
        }
    }
    
    public static boolean isLocal() {
        String env = EnvUtils.getEnv("ENV", "local");
        return "local".equalsIgnoreCase(env);
    }
}
```

## Test Example

```java
@Test
public void shouldResetBookingServiceState() {
    if (!LocalTestGuard.isLocal()) {
        throw new SkipException("Test only runs in local environment");
    }
    
    LocalTestApi testApi = new LocalTestApi(bookingClient);
    testApi.reset();
    
    // Verify state is cleared by attempting to get a booking
    Response response = bookingApi.getBooking(UUID.randomUUID().toString());
    assertThat(response.statusCode()).isEqualTo(404);
}

@Test
public void shouldInjectAndRecoverFromFailures() {
    if (!LocalTestGuard.isLocal()) {
        throw new SkipException("Test only runs in local environment");
    }
    
    LocalTestApi testApi = new LocalTestApi(bookingClient);
    
    // Enable failures
    testApi.configureFailures(FailureConfig.error(503));
    
    // Verify service returns error
    Response response = bookingApi.createBooking(validRequest);
    assertThat(response.statusCode()).isEqualTo(503);
    
    // Disable failures
    testApi.configureFailures(FailureConfig.disabled());
    
    // Verify service returns to normal
    response = bookingApi.createBooking(validRequest);
    assertThat(response.statusCode()).isIn(200, 201);
}
```

## Constraints
- Do NOT run in non-local environments
- Use TestNG's SkipException for conditional skipping
- All testkit endpoints use JSON (not XML or SOAP)
- Output diffs only
