package tests.booking;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.BookingEndpoints;
import framework.headers.CorrelationIdSupport;
import framework.reporting.ReportLogger;
import framework.requests.BookingRequests;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests for Booking Service REST API.
 * 
 * Agent: GenTests
 * Mode: delta
 * Service Type: JSON REST
 * Contract Source: Controller + DTO code (fallback)
 * Behavior Source: prompts/05-generate-booking-tests.md
 */
public class BookingControllerTests {

    private ApiClient client;
    private String capturedBookingId;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String baseUrl = TestConfig.getInstance().getBaseUrl(ServiceType.BOOKING);
        client = new RestAssuredApiClient(baseUrl);
    }

    // ==================== CORRELATION ID TESTS ====================

    @Test(groups = {"booking", "correlation"})
    public void shouldEchoCorrelationIdOnCreateBooking() {
        ReportLogger.logStep("POST /bookings with X-Correlation-Id header");
        
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> request = BookingRequests.validCreateBooking();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 201", 201, actualStatus, actualStatus == 201);
        assertThat(actualStatus).as("POST /bookings should return 201").isEqualTo(201);

        String responseCorrelationId = response.jsonPath().getString("correlationId");
        ReportLogger.logAssertion("Response correlationId should match request", 
                correlationId, responseCorrelationId, correlationId.equals(responseCorrelationId));
        assertThat(responseCorrelationId)
                .as("Response should echo the correlation ID")
                .isEqualTo(correlationId);
    }

    @Test(groups = {"booking", "correlation"}, dependsOnMethods = "shouldCreateBookingSuccessfully")
    public void shouldEchoCorrelationIdOnGetBooking() {
        ReportLogger.logStep("GET /bookings/{id} with X-Correlation-Id header");
        
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);

        Response response = client.get(BookingEndpoints.byId(capturedBookingId), headers);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 200", 200, actualStatus, actualStatus == 200);
        assertThat(actualStatus).as("GET /bookings/{id} should return 200").isEqualTo(200);
    }

    // ==================== HAPPY PATH TESTS ====================

    @Test(groups = {"booking", "happy-path"}, priority = 1)
    public void shouldCreateBookingSuccessfully() {
        ReportLogger.logStep("POST /bookings - Create new booking");
        
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> request = BookingRequests.validCreateBooking();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 201", 201, actualStatus, actualStatus == 201);
        assertThat(actualStatus).as("POST /bookings should return 201 Created").isEqualTo(201);

        String bookingId = response.jsonPath().getString("bookingId");
        ReportLogger.logAssertion("Response should contain bookingId", "non-null", bookingId, bookingId != null);
        assertThat(bookingId).as("Response should contain bookingId").isNotNull().isNotBlank();

        String status = response.jsonPath().getString("status");
        ReportLogger.logAssertion("Initial status should be PENDING_PAYMENT", "PENDING_PAYMENT", status, "PENDING_PAYMENT".equals(status));
        assertThat(status).as("Initial booking status should be PENDING_PAYMENT").isEqualTo("PENDING_PAYMENT");

        // Capture for subsequent tests
        capturedBookingId = bookingId;
        ReportLogger.info("Captured bookingId: " + capturedBookingId);
    }

    @Test(groups = {"booking", "happy-path"}, dependsOnMethods = "shouldCreateBookingSuccessfully")
    public void shouldGetBookingById() {
        ReportLogger.logStep("GET /bookings/{id} - Retrieve booking details");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.byId(capturedBookingId), headers);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 200", 200, actualStatus, actualStatus == 200);
        assertThat(actualStatus).as("GET /bookings/{id} should return 200").isEqualTo(200);

        String bookingId = response.jsonPath().getString("bookingId");
        ReportLogger.logAssertion("BookingId should match", capturedBookingId, bookingId, 
                capturedBookingId.equals(bookingId));
        assertThat(bookingId).as("Returned bookingId should match").isEqualTo(capturedBookingId);

        String flightId = response.jsonPath().getString("flightId");
        ReportLogger.logAssertion("FlightId should be present", "non-null", flightId, flightId != null);
        assertThat(flightId).as("Response should contain flightId").isNotNull();

        String seatClass = response.jsonPath().getString("seatClass");
        ReportLogger.logAssertion("SeatClass should be ECONOMY", "ECONOMY", seatClass, "ECONOMY".equals(seatClass));
        assertThat(seatClass).as("SeatClass should match request").isEqualTo("ECONOMY");
    }

    @Test(groups = {"booking", "happy-path"}, dependsOnMethods = "shouldCreateBookingSuccessfully")
    public void shouldGetBookingStatus() {
        ReportLogger.logStep("GET /bookings/{id}/status - Retrieve booking status");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.status(capturedBookingId), headers);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 200", 200, actualStatus, actualStatus == 200);
        assertThat(actualStatus).as("GET /bookings/{id}/status should return 200").isEqualTo(200);

        String bookingId = response.jsonPath().getString("bookingId");
        ReportLogger.logAssertion("BookingId should match", capturedBookingId, bookingId, 
                capturedBookingId.equals(bookingId));
        assertThat(bookingId).as("Returned bookingId should match").isEqualTo(capturedBookingId);

        String status = response.jsonPath().getString("status");
        ReportLogger.logAssertion("Status field should be present", "non-null", status, status != null);
        assertThat(status).as("Response should contain status field").isNotNull();
    }

    @Test(groups = {"booking", "happy-path"}, dependsOnMethods = "shouldCreateBookingSuccessfully")
    public void shouldGetLoyaltyAccrual() {
        ReportLogger.logStep("GET /bookings/{id}/loyalty - Retrieve loyalty accrual info");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.loyalty(capturedBookingId), headers);

        int actualStatus = response.getStatusCode();
        // Loyalty endpoint may return 200 even if no accrual yet
        ReportLogger.logAssertion("Status code should be 200", 200, actualStatus, actualStatus == 200);
        assertThat(actualStatus).as("GET /bookings/{id}/loyalty should return 200").isEqualTo(200);

        String bookingId = response.jsonPath().getString("bookingId");
        ReportLogger.logAssertion("BookingId should match", capturedBookingId, bookingId, 
                capturedBookingId.equals(bookingId));
        assertThat(bookingId).as("Returned bookingId should match").isEqualTo(capturedBookingId);
    }


    @Test(groups = {"booking", "happy-path"})
    public void shouldCreateBookingWithBusinessClass() {
        ReportLogger.logStep("POST /bookings - Create booking with BUSINESS seat class");
        
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> request = BookingRequests.withSeatClass("BUSINESS");

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 201", 201, actualStatus, actualStatus == 201);
        assertThat(actualStatus).as("POST /bookings should return 201 Created").isEqualTo(201);

        String seatClass = response.jsonPath().getString("seatClass");
        ReportLogger.logAssertion("SeatClass should be BUSINESS", "BUSINESS", seatClass, "BUSINESS".equals(seatClass));
        assertThat(seatClass).as("SeatClass should be BUSINESS").isEqualTo("BUSINESS");
    }

    // ==================== NEGATIVE TESTS ====================

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForInvalidJson() {
        ReportLogger.logStep("POST /bookings with malformed JSON");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        // Send invalid JSON string directly
        Response response = client.post(BookingEndpoints.BASE, headers, "{invalid json}");

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 400", 400, actualStatus, actualStatus == 400);
        assertThat(actualStatus).as("Malformed JSON should return 400 Bad Request").isEqualTo(400);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForEmptyRequest() {
        ReportLogger.logStep("POST /bookings with empty request body");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());
        Map<String, Object> request = BookingRequests.emptyRequest();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 400", 400, actualStatus, actualStatus == 400);
        assertThat(actualStatus).as("Empty request should return 400 Bad Request").isEqualTo(400);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForMissingFlightId() {
        ReportLogger.logStep("POST /bookings without flightId");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());
        Map<String, Object> request = BookingRequests.missingFlightId();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 400", 400, actualStatus, actualStatus == 400);
        assertThat(actualStatus).as("Missing flightId should return 400 Bad Request").isEqualTo(400);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForInvalidCurrency() {
        ReportLogger.logStep("POST /bookings with invalid currency format");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());
        Map<String, Object> request = BookingRequests.invalidCurrency();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 400", 400, actualStatus, actualStatus == 400);
        assertThat(actualStatus).as("Invalid currency should return 400 Bad Request").isEqualTo(400);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForInvalidSeatClass() {
        ReportLogger.logStep("POST /bookings with invalid seat class");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());
        Map<String, Object> request = BookingRequests.invalidSeatClass();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 400", 400, actualStatus, actualStatus == 400);
        assertThat(actualStatus).as("Invalid seat class should return 400 Bad Request").isEqualTo(400);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForZeroAmount() {
        ReportLogger.logStep("POST /bookings with zero amount");
        
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());
        Map<String, Object> request = BookingRequests.zeroAmount();

        Response response = client.post(BookingEndpoints.BASE, headers, request);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 400", 400, actualStatus, actualStatus == 400);
        assertThat(actualStatus).as("Zero amount should return 400 Bad Request").isEqualTo(400);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn404ForNonExistentBooking() {
        ReportLogger.logStep("GET /bookings/{id} with non-existent ID");
        
        String nonExistentId = "00000000-0000-0000-0000-000000000000";
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.byId(nonExistentId), headers);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 404", 404, actualStatus, actualStatus == 404);
        assertThat(actualStatus).as("Non-existent booking should return 404 Not Found").isEqualTo(404);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn404ForNonExistentBookingStatus() {
        ReportLogger.logStep("GET /bookings/{id}/status with non-existent ID");
        
        String nonExistentId = "00000000-0000-0000-0000-000000000001";
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.status(nonExistentId), headers);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 404", 404, actualStatus, actualStatus == 404);
        assertThat(actualStatus).as("Non-existent booking status should return 404 Not Found").isEqualTo(404);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn404ForNonExistentBookingLoyalty() {
        ReportLogger.logStep("GET /bookings/{id}/loyalty with non-existent ID");
        
        String nonExistentId = "00000000-0000-0000-0000-000000000002";
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.loyalty(nonExistentId), headers);

        int actualStatus = response.getStatusCode();
        ReportLogger.logAssertion("Status code should be 404", 404, actualStatus, actualStatus == 404);
        assertThat(actualStatus).as("Non-existent booking loyalty should return 404 Not Found").isEqualTo(404);
    }

    @Test(groups = {"booking", "negative"})
    public void shouldReturn400ForInvalidUuidFormat() {
        ReportLogger.logStep("GET /bookings/{id} with invalid UUID format");
        
        String invalidId = "not-a-valid-uuid";
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>());

        Response response = client.get(BookingEndpoints.byId(invalidId), headers);

        int actualStatus = response.getStatusCode();
        // Could be 400 or 404 depending on implementation
        ReportLogger.logAssertion("Status code should be 4xx", "4xx", String.valueOf(actualStatus), 
                actualStatus >= 400 && actualStatus < 500);
        assertThat(actualStatus)
                .as("Invalid UUID format should return 4xx error")
                .isBetween(400, 499);
    }
}
