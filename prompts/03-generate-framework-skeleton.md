# Prompt 03 — Generate Framework Skeleton (JSON REST + XML REST + SOAP)

Follow TEST_GENERATION_BLUEPRINT.md strictly.
Follow 00-agent-operating-rules.md strictly.

You are acting as the **GenFramework agent**.

Your responsibility is LIMITED to creating or evolving the API test framework.
You MUST NOT generate service-specific functional tests.

You are allowed to read repository files.
You are NOT allowed to run build, test, or system commands.
Output ONLY diffs.

--------------------------------
PARAMETERS
--------------------------------
- **TYPE**: testng (default) | bdd | all

| TYPE | Description | What Gets Created |
|------|-------------|-------------------|
| `testng` | Traditional TestNG framework only | Core framework + smoke tests (no `bdd/` package, no `features/`) |
| `bdd` | BDD/Cucumber infrastructure only | `bdd/` package + `features/` directories (assumes core framework exists) |
| `all` | Both TestNG and BDD | Everything - core framework, smoke tests, BDD infrastructure |

--------------------------------
MODULE BOOTSTRAP RULE
--------------------------------
- If `api-tests` module does NOT exist:
    - Create it fully as described below based on TYPE parameter
- If `api-tests` module already exists:
    - Modify ONLY the required framework classes based on TYPE
    - Do NOT recreate the module or overwrite existing files unless necessary

--------------------------------
DEPENDENCIES BY TYPE
--------------------------------

### TYPE=testng (default)
Create `api-tests/build.gradle` (Groovy DSL) with:
```groovy
plugins {
    id 'java'
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    testImplementation 'io.rest-assured:rest-assured:5.5.0'
    testImplementation 'org.testng:testng:7.10.2'
    testImplementation 'org.assertj:assertj-core:3.26.3'
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.17.2'
    testImplementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2'
    testImplementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.2'
    testImplementation 'com.aventstack:extentreports:5.1.2'
    testImplementation 'org.slf4j:slf4j-simple:2.0.16'
}

tasks.test {
    useTestNG {
        suites 'src/test/resources/testng.xml'
    }
}
```

### TYPE=bdd (add to existing build.gradle)

**AUTO-BOOTSTRAP**: If core framework doesn't exist, create it first.

Check if these files exist:
- `api-tests/build.gradle`
- `api-tests/src/test/java/framework/clients/ApiClient.java`
- `api-tests/src/test/java/framework/config/TestConfig.java`

If these files do NOT exist:
1. First, create the FULL core framework (same as TYPE=testng section above)
2. Include Cucumber dependencies in the initial build.gradle
3. Then proceed with BDD infrastructure below

If these files already exist, add these dependencies to existing build.gradle:
```groovy
    // JUnit 5 Platform for Cucumber
    testImplementation platform('org.junit:junit-bom:5.10.3')
    testImplementation 'org.junit.platform:junit-platform-suite'
    testImplementation 'org.junit.jupiter:junit-jupiter'
    
    // Cucumber with JUnit Platform (industry standard)
    testImplementation 'io.cucumber:cucumber-java:7.18.1'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.18.1'
    testImplementation 'io.cucumber:cucumber-picocontainer:7.18.1'
    
    // ExtentReports Cucumber Adapter
    testImplementation 'tech.grasshopper:extentreports-cucumber7-adapter:1.14.0'
```

### TYPE=all
Include ALL dependencies from both testng and bdd:
```groovy
plugins {
    id 'java'
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    // REST API Testing
    testImplementation 'io.rest-assured:rest-assured:5.5.0'
    
    // TestNG for smoke tests
    testImplementation 'org.testng:testng:7.10.2'
    
    // JUnit 5 Platform for Cucumber
    testImplementation platform('org.junit:junit-bom:5.10.3')
    testImplementation 'org.junit.platform:junit-platform-suite'
    testImplementation 'org.junit.jupiter:junit-jupiter'
    
    // Cucumber with JUnit Platform (industry standard)
    testImplementation 'io.cucumber:cucumber-java:7.18.1'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.18.1'
    testImplementation 'io.cucumber:cucumber-picocontainer:7.18.1'
    
    // Assertions
    testImplementation 'org.assertj:assertj-core:3.26.3'
    
    // JSON/XML Processing
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.17.2'
    testImplementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2'
    testImplementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.2'
    
    // Reporting
    testImplementation 'com.aventstack:extentreports:5.1.2'
    testImplementation 'tech.grasshopper:extentreports-cucumber7-adapter:1.14.0'
    
    // Logging
    testImplementation 'org.slf4j:slf4j-simple:2.0.16'
}

// Smoke tests use TestNG
tasks.register('smokeTest', Test) {
    description = 'Runs smoke tests with TestNG'
    group = 'verification'
    useTestNG {
        suites 'src/test/resources/testng.xml'
    }
    testClassesDirs = sourceSets.test.output.classesDirs
    classpath = sourceSets.test.runtimeClasspath
}

// Cucumber BDD tests use JUnit Platform
tasks.register('cucumberTest', Test) {
    description = 'Runs Cucumber BDD tests with JUnit Platform'
    group = 'verification'
    useJUnitPlatform()
    include '**/CucumberTest.class'
    systemProperty 'cucumber.junit-platform.naming-strategy', 'long'
}

// Default test task runs both
tasks.test {
    dependsOn smokeTest, cucumberTest
    enabled = false
}
```

--------------------------------
FRAMEWORK STRUCTURE BY TYPE
--------------------------------

## TYPE=testng - Core Framework Packages

Create these packages under `api-tests/src/test/java/framework/`:

### A) framework/config
- ServiceType (enum)
    - BOOKING, INVENTORY, PAYMENT, BAGGAGE, LOYALTY

- TestConfig
    - reads environment variables:
        - BASE_URL_BOOKING (default: http://localhost:8081)
        - BASE_URL_INVENTORY (default: http://localhost:8082)
        - BASE_URL_PAYMENT (default: http://localhost:8083)
        - BASE_URL_LOYALTY (default: http://localhost:8084)
        - BASE_URL_BAGGAGE (default: http://localhost:8085)
        - ENV (local/dev/qa)
        - LOG_HTTP (true/false)
    - fail fast if required values are missing
    - provide getters for each service URL
    - getBaseUrl(ServiceType service) → returns URL for given service type

## B) framework/clients
- ApiClient (interface)
    - Response get(String path, Map<String, String> headers)
    - Response post(String path, Map<String, String> headers, Object body)
    - Response put(String path, Map<String, String> headers, Object body)
    - Response patch(String path, Map<String, String> headers, Object body)
    - Response delete(String path, Map<String, String> headers)

- RestAssuredApiClient (JSON REST implementation)
    - implements ApiClient
    - bound to a SINGLE base URL per instance
    - sets Content-Type: application/json
    - sets Accept: application/json
    - RestAssured MUST be used only here
    - logs requests/responses when LOG_HTTP=true
    - MUST call ReportLogger.logRequest() before each HTTP call
    - MUST call ReportLogger.logResponse() after each HTTP call
    - This enables automatic request/response logging in ExtentReports

## C) framework/xml
- XmlApiClient (XML REST implementation)
    - implements ApiClient interface
    - bound to a SINGLE base URL per instance
    - sets Content-Type: application/xml
    - sets Accept: application/xml
    - uses XmlUtils for serialization/deserialization
    - logs requests/responses when LOG_HTTP=true

- XmlRequestBuilder (abstract base class)
    - protected String namespace
    - withNamespace(String ns) → returns this
    - abstract String build() → returns XML string

- BaggageCheckinXmlBuilder extends XmlRequestBuilder
    - withBookingId(String) - required
    - withPassengerId(String) - required
    - withBagTag(String) - required, pattern [A-Z]{2}[0-9]{8}
    - withOrigin(String) - required, 3-letter airport code
    - withDestination(String) - required, 3-letter airport code
    - build() → XML string with namespace

- BaggageStatusUpdateXmlBuilder extends XmlRequestBuilder
    - withStatus(String)
    - withLocation(String)
    - build() → XML string with namespace

## D) framework/soap
- SoapClient (interface)
    - SoapResponse sendRequest(String soapAction, String envelope)

- SoapClientImpl
    - implements SoapClient
    - bound to a SINGLE base URL per instance (e.g., http://localhost:8085)
    - sends POST to /ws endpoint
    - sets Content-Type: text/xml; charset=utf-8
    - sets SOAPAction header
    - parses SOAP responses
    - handles SOAP faults
    - logs requests/responses when LOG_HTTP=true

- SoapEnvelopeBuilder
    - private String namespace
    - private String bodyContent
    - withNamespace(String ns) → returns this
    - withBody(String body) → returns this
    - build() → complete SOAP envelope string

- SoapResponseParser
    - static String extractBody(String envelope) → body content
    - static SoapFault extractFault(String envelope) → SoapFault or null
    - static <T> T extractElement(String envelope, String elementName, Class<T> type)

- SoapResponse (model)
    - int statusCode
    - String rawResponse
    - boolean isFault()
    - String getBody()
    - SoapFault getFault()

- SoapFault (model)
    - String faultCode
    - String faultString
    - String faultDetail
    - String loyaltyFaultCode (from detail)
    - String loyaltyFaultMessage (from detail)

- LoyaltySoapRequestBuilder
    - static String enrollMember(String firstName, String lastName, String email)
    - static String getMemberStatus(String memberId)
    - static String accruePoints(String memberId, String bookingId, String amount, String currency, String correlationId)

## E) framework/endpoints
- BookingEndpoints
    - static final String API_DOCS = "/v3/api-docs"
    - static final String BASE = "/bookings"
    - static final String BY_ID = "/bookings/{id}"
    - static final String STATUS = "/bookings/{id}/status"
    - static final String LOYALTY = "/bookings/{id}/loyalty"

- InventoryEndpoints
    - static final String API_DOCS = "/v3/api-docs"
    - static final String RESERVATIONS = "/inventory/reservations/{reservationId}"
    - static final String BY_BOOKING = "/inventory/reservations/by-booking/{bookingId}"
    - static final String ADMIN_SEED = "/inventory/admin/seed"
    - static final String ADMIN_RESET = "/inventory/admin/reset"

- PaymentEndpoints
    - static final String API_DOCS = "/v3/api-docs"

- BaggageEndpoints
    - static final String HEALTH = "/actuator/health"
    - static final String CHECKIN = "/baggage/checkin"
    - static final String STATUS = "/baggage/status/{bagTag}"
    - static final String TRACK = "/baggage/track/{bagTag}"
    - static final String ADMIN_SEED = "/baggage/admin/seed"

- LoyaltyEndpoints
    - static final String SOAP_ENDPOINT = "/ws"
    - static final String WSDL = "/ws/loyalty.wsdl"
    - static final String ADMIN_SEED = "/loyalty/admin/seed"
    - static final String ADMIN_RESET = "/loyalty/admin/reset"
    - static final String SOAP_ACTION_ENROLL = "http://letzautomate.com/loyalty/v1/EnrollMember"
    - static final String SOAP_ACTION_STATUS = "http://letzautomate.com/loyalty/v1/GetMemberStatus"
    - static final String SOAP_ACTION_ACCRUE = "http://letzautomate.com/loyalty/v1/AccruePoints"

- TestkitEndpoints
    - static final String RESET = "/test/reset"
    - static final String FAILURES = "/test/failures"
    - static final String EVENTS = "/test/events"

## F) framework/requests
- BookingRequests
    - static Map<String, Object> validCreateBooking()
    - static Map<String, Object> createBooking(String flightId, String passengerId, String seatClass)

- InventoryRequests
    - static Map<String, Object> seedInventory(String flightId, String seatClass, int availableSeats)

## G) framework/models

### framework/models/request
- CreateBookingRequest (record or class)
- SeedInventoryRequest (record or class)

### framework/models/response
- BookingResponse
- BookingStatusResponse
- InventoryReservationResponse
- BaggageCheckinResponse
- BaggageTrackResponse
- LoyaltyEnrollResponse
- LoyaltyStatusResponse
- LoyaltyAccrueResponse

### framework/models/common
- ErrorResponse
    - String timestamp
    - int status
    - String error
    - String message
    - String path
    - String correlationId

- SoapFault (already in soap package)

## H) framework/mappers
- ErrorResponseMapper
    - static ErrorResponse fromResponse(Response response)
    - static ErrorResponse fromJson(String json)

- XmlResponseMapper
    - static <T> T fromXml(String xml, Class<T> type)
    - static <T> T fromResponse(Response response, Class<T> type)

## I) framework/asserters
- ErrorAsserter
    - static void assertValidErrorResponse(ErrorResponse error)
    - static void assertStatusCode(ErrorResponse error, int expectedStatus)
    - static void assertCorrelationId(ErrorResponse error, String expectedCorrelationId)
    - static void assertMessageContains(ErrorResponse error, String substring)

- SoapFaultAsserter
    - static void assertValidSoapFault(SoapFault fault)
    - static void assertFaultCode(SoapFault fault, String expectedCode)
    - static void assertFaultMessageContains(SoapFault fault, String substring)

- XmlResponseAsserter
    - static void assertValidXmlResponse(Response response)
    - static void assertXmlElementPresent(String xml, String elementName)
    - static void assertXmlElementValue(String xml, String elementName, String expectedValue)

## J) framework/headers
- CorrelationIdSupport
    - static String generate() → UUID string
    - static Map<String, String> withCorrelationId(Map<String, String> headers)
    - static Map<String, String> withCorrelationId(Map<String, String> headers, String correlationId)
    - static final String HEADER_NAME = "X-Correlation-Id"

- IdempotencyKeySupport
    - static String generate() → UUID string
    - static Map<String, String> withIdempotencyKey(Map<String, String> headers)
    - static Map<String, String> withIdempotencyKey(Map<String, String> headers, String key)
    - static final String HEADER_NAME = "Idempotency-Key"

## K) framework/utils
- EnvUtils
    - static String getEnv(String name, String defaultValue)
    - static String requireEnv(String name)
    - static boolean isLocal()

- UuidUtils
    - static String generate()
    - static boolean isValid(String uuid)

- JsonUtils
    - static String toJson(Object obj)
    - static String toPrettyJson(Object obj) → indented JSON for reports
    - static String prettyPrint(String json) → formats existing JSON string
    - static <T> T fromJson(String json, Class<T> type)

- XmlUtils
    - static String toXml(Object obj)
    - static String toXml(Object obj, String namespace)
    - static <T> T fromXml(String xml, Class<T> type)
    - static String wrapWithNamespace(String content, String rootElement, String namespace)

## L) framework/testkit
- LocalTestClient
    - private final ApiClient client
    - constructor checks ENV == local, throws if not
    - void reset()
    - void configureFailures(Map<String, Object> config)
    - List<Map<String, Object>> events()

- LocalTestGuard
    - static void ensureLocal() → throws if ENV != local
    - static boolean isLocal()

- FailureConfig
    - static FailureConfig timeout(int ms)
    - static FailureConfig error(int statusCode)
    - static FailureConfig latency(int ms)
    - static FailureConfig disabled()
    - Map<String, Object> toMap()

## M) framework/kafka (for Kafka event testing)
- KafkaTestConfig
    - static String getBootstrapServers() → KAFKA_BOOTSTRAP_SERVERS or localhost:9092
    - static int getDefaultTimeoutSeconds() → KAFKA_TIMEOUT_SECONDS or 10
    - static int getSagaTimeoutSeconds() → KAFKA_SAGA_TIMEOUT_SECONDS or 30

- ConsumedEvent (record)
    - String topic
    - String key
    - String rawValue
    - JsonNode json
    - String eventType (from meta.eventType)
    - String correlationId (from meta.correlationId)
    - long timestamp
    - JsonNode getData() → returns data section
    - JsonNode getMeta() → returns meta section
    - String getEventId() → returns meta.eventId
    - String getProducer() → returns meta.producer

- TestKafkaConsumer (implements AutoCloseable)
    - constructor(String topic) or constructor(List<String> topics)
    - creates consumer with unique group ID per test run
    - AUTO_OFFSET_RESET_CONFIG = "earliest"
    - void clearBuffer() → clears consumed events
    - Optional<ConsumedEvent> waitForEvent(String correlationId, Duration timeout)
    - Optional<ConsumedEvent> waitForEventOfType(String eventType, String correlationId, Duration timeout)
    - List<ConsumedEvent> getAllEvents()
    - List<ConsumedEvent> getEventsByCorrelationId(String correlationId)
    - void close() → stops polling thread and closes consumer

## N) framework/reporting
- ExtentReportManager (singleton)
    - static ExtentReports getInstance()
    - static ExtentTest createTest(String testName)
    - static ExtentTest createTest(String testName, String description)
    - static ExtentTest getTest() → returns current thread's test
    - static void flush()
    - static void removeTest()
    - Report path: build/reports/extent/extent-report-{timestamp}.html
    - System info: Environment, Java Version, OS

- ExtentTestListener (implements ITestListener)
    - onStart(ITestContext) → initialize report
    - onFinish(ITestContext) → flush report
    - onTestStart(ITestResult) → create test with name and groups
    - onTestSuccess(ITestResult) → log PASS
    - onTestFailure(ITestResult) → log FAIL with exception
    - onTestSkipped(ITestResult) → log SKIP

- ReportLogger (utility class for detailed logging)
    - static void logRequest(String method, String baseUrl, String path, Map headers, Object body)
        - Logs: 🔵 REQUEST with Method, URL, Headers, Payload (if applicable)
        - Uses HTML formatting with styled cards and tables
        - Pretty-prints JSON payloads using JsonUtils.toPrettyJson()
        - Uses ExtentReports CodeBlock with syntax highlighting
    - static void logResponse(Response response)
        - Logs: 🟢 RESPONSE with Status Code, Status Line, Response Time, Headers, Body
        - Color-codes status: green (2xx), red (4xx/5xx), orange (3xx)
        - Pretty-prints JSON responses with proper indentation
        - Formats XML responses with basic indentation
        - Uses CodeLanguage.JSON or CodeLanguage.XML for syntax highlighting
        - Truncates large responses (>5000 chars) for readability
    - static void logAssertion(String description, Object expected, Object actual, boolean passed)
        - Logs: ✅/❌ ASSERTION with Expected vs Actual comparison
        - Uses styled cards with green/red borders based on result
        - Shows Expected and Actual values in code blocks
        - Calls test.pass() or test.fail() based on result
    - static void logStep(String stepDescription)
        - Logs: 📋 STEP description with orange accent styling
    - static void info(String message)
        - Logs simple info message with icon
    - IMPORTANT: RestAssuredApiClient MUST call logRequest/logResponse automatically
    - Tests MUST call logAssertion for each assertion to show Expected vs Actual comparison
    - JSON bodies are automatically pretty-printed for readability
    - XML bodies are formatted with basic indentation

## N) framework/bdd (TYPE=bdd or TYPE=all ONLY)

**Skip this section if TYPE=testng**

- TestContext
    - Shared context for Cucumber step definitions
    - Stores: headers, lastResponse, lastSoapResponse, lastRequestBody
    - Methods: setClient(), getClient(), setXmlClient(), setSoapClient(), getSoapClient()
    - Methods: setHeader(), getHeaders(), clearHeaders()
    - Methods: set(key, value), get(key), getString(key)
    - Method: reset() → clears all state for new scenario
    - Note: setClient() always uses RestAssuredApiClient (JSON)
    - Note: setXmlClient() switches to XmlApiClient for XML requests

- CommonStepDefinitions
    - Implements canonical step vocabulary from GHERKIN_STYLE_GUIDE.md
    - Service setup steps: "Given I am testing the {service} service"
    - Header steps: "Given I set header {name} to {value}"
    - HTTP call steps: "When I call {method} {path}"
    - Assertion steps: "Then the response status should be {status}"
    - Capture steps: "And I capture {jsonPath} as {var}"

- CucumberTest (JUnit 5 Platform Suite - industry standard)
    - Uses @Suite, @IncludeEngines("cucumber"), @SelectClasspathResource("features")
    - @ConfigurationParameter for glue, plugins, tags
    - Integrates with ExtentReports via extentreports-cucumber7-adapter
    - Parallel execution support via @DataProvider

--------------------------------
SMOKE TESTS (TYPE=testng or TYPE=all ONLY)
--------------------------------

**Skip this section if TYPE=bdd**

Create smoke tests under `api-tests/src/test/java/tests/smoke/`:

IMPORTANT: 
- All @BeforeClass methods MUST use `alwaysRun = true` to work with TestNG groups
- All tests MUST use ReportLogger for detailed reporting
- Tests MUST use ReportLogger.logStep() to describe what they're doing
- Tests MUST use ReportLogger.logAssertion() to show Expected vs Actual comparison

Example smoke test pattern:
```java
package tests.smoke;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.BookingEndpoints;
import framework.reporting.ReportLogger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingOpenApiSmokeTest {
    private ApiClient client;
    
    @BeforeClass(alwaysRun = true)
    public void setup() {
        String baseUrl = TestConfig.getInstance().getBaseUrl(ServiceType.BOOKING);
        client = new RestAssuredApiClient(baseUrl);
    }
    
    @Test(groups = "smoke")
    public void apiDocsShouldReturn200() {
        ReportLogger.logStep("Calling Booking Service OpenAPI docs endpoint");
        var response = client.get(BookingEndpoints.API_DOCS, Collections.emptyMap());
        
        int actualStatus = response.getStatusCode();
        int expectedStatus = 200;
        boolean passed = actualStatus == expectedStatus;
        
        ReportLogger.logAssertion("Status code should be 200", expectedStatus, actualStatus, passed);
        
        assertThat(actualStatus)
                .as("GET /v3/api-docs should return 200")
                .isEqualTo(expectedStatus);
    }
}
```

1) `BookingOpenApiSmokeTest.java`
    - Reads BASE_URL_BOOKING via TestConfig.getInstance().getBaseUrl(ServiceType.BOOKING)
    - Calls GET `/v3/api-docs` using BookingEndpoints.API_DOCS
    - Uses ReportLogger.logStep() and ReportLogger.logAssertion()
    - Asserts status code 200

2) `InventoryOpenApiSmokeTest.java`
    - Reads BASE_URL_INVENTORY via TestConfig.getInstance().getBaseUrl(ServiceType.INVENTORY)
    - Calls GET `/v3/api-docs` using InventoryEndpoints.API_DOCS
    - Uses ReportLogger.logStep() and ReportLogger.logAssertion()
    - Asserts status code 200

3) `PaymentOpenApiSmokeTest.java`
    - Reads BASE_URL_PAYMENT via TestConfig.getInstance().getBaseUrl(ServiceType.PAYMENT)
    - Calls GET `/v3/api-docs` using PaymentEndpoints.API_DOCS
    - Uses ReportLogger.logStep() and ReportLogger.logAssertion()
    - Asserts status code 200

4) `BaggageOpenApiSmokeTest.java`
    - Reads BASE_URL_BAGGAGE via TestConfig.getInstance().getBaseUrl(ServiceType.BAGGAGE)
    - Calls GET `/actuator/health` using BaggageEndpoints.HEALTH
    - Note: Baggage service doesn't expose OpenAPI docs (no springdoc dependency)
    - Uses ReportLogger.logStep() and ReportLogger.logAssertion()
    - Asserts status code 200

5) `LoyaltyWsdlSmokeTest.java`
    - Reads BASE_URL_LOYALTY via TestConfig.getInstance().getBaseUrl(ServiceType.LOYALTY)
    - Calls GET `/ws/loyalty.wsdl` using LoyaltyEndpoints.WSDL
    - Uses ReportLogger.logStep() and ReportLogger.logAssertion()
    - Asserts status code 200
    - Asserts response body contains "wsdl:definitions" or "definitions"

--------------------------------
RESOURCE DIRECTORIES BY TYPE
--------------------------------

### TYPE=testng
Create these directories:
- api-tests/src/test/resources/openapi-snapshots/booking-service/
- api-tests/src/test/resources/openapi-snapshots/inventory-service/
- api-tests/src/test/resources/openapi-snapshots/payment-service/
- api-tests/src/test/resources/openapi-snapshots/baggage-service/
- api-tests/src/test/resources/wsdl-snapshots/loyalty-service/

### TYPE=bdd
Create these directories:
- api-tests/src/test/resources/features/booking-service/
- api-tests/src/test/resources/features/inventory-service/
- api-tests/src/test/resources/features/payment-service/
- api-tests/src/test/resources/features/baggage-service/
- api-tests/src/test/resources/features/loyalty-service/

### TYPE=all
Create ALL directories from both testng and bdd.

--------------------------------
TESTNG CONFIGURATION (TYPE=testng or TYPE=all)
--------------------------------

**Skip this section if TYPE=bdd**

Create `api-tests/src/test/resources/testng.xml`:
- Register ExtentTestListener
- Define test suites for smoke tests
- Use explicit class names (not packages) for reliable test discovery

IMPORTANT: The testng.xml file MUST be referenced in build.gradle:
```groovy
tasks.test {
    useTestNG {
        suites 'src/test/resources/testng.xml'
    }
}
```

Example structure:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="API Test Suite" verbose="1">
    <listeners>
        <listener class-name="framework.reporting.ExtentTestListener"/>
    </listeners>
    
    <test name="Smoke Tests">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="tests.smoke.BookingOpenApiSmokeTest"/>
            <class name="tests.smoke.InventoryOpenApiSmokeTest"/>
            <class name="tests.smoke.PaymentOpenApiSmokeTest"/>
            <class name="tests.smoke.BaggageOpenApiSmokeTest"/>
            <class name="tests.smoke.LoyaltyWsdlSmokeTest"/>
        </classes>
    </test>
</suite>
```

--------------------------------
GHERKIN FEATURE FILES (TYPE=bdd or TYPE=all ONLY)
--------------------------------

**Skip this section if TYPE=testng**

Create feature file directories:
- api-tests/src/test/resources/features/booking-service/
- api-tests/src/test/resources/features/inventory-service/
- api-tests/src/test/resources/features/payment-service/
- api-tests/src/test/resources/features/baggage-service/
- api-tests/src/test/resources/features/loyalty-service/

Create sample feature files following GHERKIN_STYLE_GUIDE.md:
- Use service tags (@booking, @inventory, @baggage, @loyalty)
- Use service type tags (@xmlRest, @soap) where applicable
- Use scenario ID tags (@id=SERVICE-NNN)
- Use category tags (@smoke, @happyPath, @negative)

--------------------------------
EXTENT REPORTS CONFIGURATION (TYPE=bdd or TYPE=all ONLY)
--------------------------------

**Skip this section if TYPE=testng**

Create `api-tests/src/test/resources/extent.properties`:
```properties
extent.reporter.spark.start=true
extent.reporter.spark.out=build/reports/cucumber/extent-cucumber-report.html
extent.reporter.spark.config=src/test/resources/extent-config.xml

screenshot.dir=build/reports/cucumber/screenshots/
screenshot.rel.path=screenshots/

systeminfo.Environment=local
systeminfo.Application=Flight Booking Platform
systeminfo.OS=${os.name}
systeminfo.Java=${java.version}
```

Create `api-tests/src/test/resources/extent-config.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<extentreports>
    <configuration>
        <theme>STANDARD</theme>
        <documentTitle>API Test Automation Report</documentTitle>
        <reportName>Flight Booking Platform - BDD Tests</reportName>
    </configuration>
</extentreports>
```

--------------------------------
STRICT RULES
--------------------------------
- Respect TYPE parameter: only create components specified for that TYPE
- Do NOT create `bdd/` package or `features/` directories if TYPE=testng
- Do NOT create smoke tests if TYPE=bdd (assumes they already exist)
- Do NOT create service-specific functional tests (only smoke tests for TYPE=testng/all)
- Do NOT access domain/application packages
- Do NOT leave methods unimplemented
- Do NOT invent additional abstractions beyond what's specified
- Follow SRP exactly
- XML REST uses XmlApiClient
- SOAP uses SoapClient
- JSON REST uses RestAssuredApiClient
- ALWAYS use @BeforeClass(alwaysRun = true) when tests use groups
- ALWAYS configure Gradle to use testng.xml suite file for ExtentReports
- Use JUnit 5 Platform for Cucumber (industry standard), NOT TestNG
- Use cucumber-picocontainer for dependency injection in step definitions


--------------------------------
VERIFICATION (After Framework Creation)
--------------------------------
After creating the framework, run the smoke tests to verify everything works:

```bash
./gradlew :api-tests:test
```

Expected results:
- All 5 smoke tests should PASS
- ExtentReport should be generated at: `api-tests/build/reports/extent/extent-report-{timestamp}.html`
- Report should contain detailed request/response logging for each test

If tests fail:
1. Verify all services are running (ports 8081-8085)
2. Check endpoint paths match actual service endpoints
3. Verify testng.xml references correct test classes
4. Ensure @BeforeClass has alwaysRun = true

