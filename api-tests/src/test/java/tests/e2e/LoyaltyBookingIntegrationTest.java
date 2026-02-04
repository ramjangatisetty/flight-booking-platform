package tests.e2e;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.BookingEndpoints;
import framework.endpoints.InventoryEndpoints;
import framework.endpoints.LoyaltyEndpoints;
import framework.headers.CorrelationIdSupport;
import framework.reporting.ReportLogger;
import framework.requests.BookingRequests;
import framework.requests.InventoryRequests;
import framework.soap.LoyaltySoapRequestBuilder;
import framework.soap.SoapClient;
import framework.soap.SoapClientImpl;
import framework.soap.SoapResponse;
import framework.soap.SoapResponseParser;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cross-service integration tests for Loyalty + Booking flow.
 * 
 * Tests the integration between:
 * - Loyalty Service (SOAP) - member enrollment and points accrual
 * - Booking Service (REST) - booking creation with memberId
 * 
 * Flow:
 * 1. Enroll member via Loyalty SOAP
 * 2. Create booking with memberId via Booking REST
 * 3. Wait for saga completion (CONFIRMED)
 * 4. Verify loyalty points accrued
 * 
 * Prerequisites:
 * - All services running (booking:8081, inventory:8082, payment:8083, loyalty:8084)
 * - Kafka running for event propagation
 */
public class LoyaltyBookingIntegrationTest {

    private ApiClient bookingClient;
    private ApiClient inventoryClient;
    private SoapClient loyaltyClient;
    
    private static final String TEST_FLIGHT_ID = "LOYALTY-FL001";
    private static final int POLL_INTERVAL_MS = 500;
    private static final int MAX_POLL_ATTEMPTS = 20;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        TestConfig config = TestConfig.getInstance();
        bookingClient = new RestAssuredApiClient(config.getBaseUrl(ServiceType.BOOKING));
        inventoryClient = new RestAssuredApiClient(config.getBaseUrl(ServiceType.INVENTORY));
        loyaltyClient = new SoapClientImpl(config.getBaseUrl(ServiceType.LOYALTY));
    }

    @Test(groups = {"e2e", "integration", "loyalty"})
    public void shouldAccrueLoyaltyPointsWhenBookingConfirmed() throws InterruptedException {
        ReportLogger.logStep("=== E2E INTEGRATION TEST: Loyalty Points Accrual ===");
        
        // Step 1: Enroll a new loyalty member via SOAP
        ReportLogger.logStep("Step 1: Enrolling new loyalty member via SOAP");
        String firstName = "Test";
        String lastName = "Member" + System.currentTimeMillis();
        String email = "test" + System.currentTimeMillis() + "@example.com";
        
        String enrollEnvelope = LoyaltySoapRequestBuilder.enrollMember(firstName, lastName, email);
        SoapResponse enrollResponse = loyaltyClient.sendRequest(
                LoyaltyEndpoints.SOAP_ACTION_ENROLL, enrollEnvelope);
        
        ReportLogger.logAssertion("Enroll should return 200", 200, enrollResponse.getStatusCode(),
                enrollResponse.getStatusCode() == 200);
        assertThat(enrollResponse.getStatusCode())
                .as("SOAP EnrollMember should return 200")
                .isEqualTo(200);
        
        String memberId = SoapResponseParser.extractElement(enrollResponse.getBody(), "memberId", String.class);
        ReportLogger.info("Enrolled member: " + memberId);
        assertThat(memberId).as("MemberId should be returned").isNotNull().isNotBlank();
        
        // Step 2: Seed inventory for the test flight
        ReportLogger.logStep("Step 2: Seeding inventory for flight " + TEST_FLIGHT_ID);
        Map<String, Object> seedRequest = InventoryRequests.seedInventory(TEST_FLIGHT_ID, "ECONOMY", 10);
        Response seedResponse = inventoryClient.post(InventoryEndpoints.ADMIN_SEED, new HashMap<>(), seedRequest);
        
        assertThat(seedResponse.getStatusCode())
                .as("Inventory seeding should return 200")
                .isEqualTo(200);
        
        // Step 3: Create booking WITH memberId
        ReportLogger.logStep("Step 3: Creating booking with memberId for loyalty accrual");
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> bookingRequest = BookingRequests.createBooking(
                TEST_FLIGHT_ID, "ECONOMY", new java.math.BigDecimal("299.99"), "USD", memberId);
        
        Response createResponse = bookingClient.post(BookingEndpoints.BASE, headers, bookingRequest);
        
        ReportLogger.logAssertion("Booking creation should return 201", 201, createResponse.getStatusCode(),
                createResponse.getStatusCode() == 201);
        assertThat(createResponse.getStatusCode())
                .as("Booking creation should return 201 Created")
                .isEqualTo(201);
        
        String bookingId = createResponse.jsonPath().getString("bookingId");
        String responseMemberId = createResponse.jsonPath().getString("memberId");
        
        ReportLogger.info("Created booking: " + bookingId + " with memberId: " + responseMemberId);
        assertThat(bookingId).as("BookingId should be present").isNotNull();
        assertThat(responseMemberId).as("MemberId should be echoed in response").isEqualTo(memberId);
        
        // Step 4: Wait for booking to be CONFIRMED
        ReportLogger.logStep("Step 4: Waiting for booking confirmation (saga completion)");
        String finalStatus = pollForTerminalStatus(bookingId, correlationId);
        
        ReportLogger.logAssertion("Final status should be CONFIRMED", "CONFIRMED", finalStatus,
                "CONFIRMED".equals(finalStatus));
        assertThat(finalStatus)
                .as("Booking should reach CONFIRMED status")
                .isEqualTo("CONFIRMED");
        
        // Step 5: Verify loyalty accrual in booking response
        ReportLogger.logStep("Step 5: Verifying loyalty accrual status in booking");
        
        // Give a bit more time for async loyalty accrual
        Thread.sleep(2000);
        
        Response bookingResponse = bookingClient.get(BookingEndpoints.byId(bookingId), headers);
        assertThat(bookingResponse.getStatusCode()).isEqualTo(200);
        
        String loyaltyAccrualStatus = bookingResponse.jsonPath().getString("loyaltyAccrualStatus");
        Integer loyaltyPoints = bookingResponse.jsonPath().getInt("loyaltyPoints");
        
        ReportLogger.info("Loyalty accrual status: " + loyaltyAccrualStatus);
        ReportLogger.info("Loyalty points: " + loyaltyPoints);
        
        ReportLogger.logAssertion("Loyalty accrual should succeed", "SUCCEEDED", loyaltyAccrualStatus,
                "SUCCEEDED".equals(loyaltyAccrualStatus));
        assertThat(loyaltyAccrualStatus)
                .as("Loyalty accrual status should be SUCCEEDED")
                .isEqualTo("SUCCEEDED");
        
        assertThat(loyaltyPoints)
                .as("Loyalty points should be credited")
                .isNotNull()
                .isGreaterThan(0);
        
        // Step 6: Verify points via Loyalty SOAP GetMemberStatus
        ReportLogger.logStep("Step 6: Verifying points balance via Loyalty SOAP");
        String statusEnvelope = LoyaltySoapRequestBuilder.getMemberStatus(memberId);
        SoapResponse statusResponse = loyaltyClient.sendRequest(
                LoyaltyEndpoints.SOAP_ACTION_STATUS, statusEnvelope);
        
        assertThat(statusResponse.getStatusCode()).isEqualTo(200);
        
        String pointsBalance = SoapResponseParser.extractElement(statusResponse.getBody(), "pointsBalance", String.class);
        ReportLogger.info("Member points balance from Loyalty service: " + pointsBalance);
        
        assertThat(Integer.parseInt(pointsBalance))
                .as("Points balance should be greater than 0")
                .isGreaterThan(0);
        
        ReportLogger.pass("✅ Loyalty integration test passed: Member enrolled → Booking created → Points accrued");
    }

    @Test(groups = {"e2e", "integration", "loyalty"})
    public void shouldNotAccruePointsWhenNoMemberId() throws InterruptedException {
        ReportLogger.logStep("=== E2E INTEGRATION TEST: No Loyalty Accrual Without MemberId ===");
        
        String flightId = "NOLOYALTY-FL" + System.currentTimeMillis();
        
        // Step 1: Seed inventory
        ReportLogger.logStep("Step 1: Seeding inventory");
        Map<String, Object> seedRequest = InventoryRequests.seedInventory(flightId, "ECONOMY", 10);
        inventoryClient.post(InventoryEndpoints.ADMIN_SEED, new HashMap<>(), seedRequest);
        
        // Step 2: Create booking WITHOUT memberId
        ReportLogger.logStep("Step 2: Creating booking WITHOUT memberId");
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> bookingRequest = BookingRequests.withFlightId(flightId);
        
        Response createResponse = bookingClient.post(BookingEndpoints.BASE, headers, bookingRequest);
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        
        String bookingId = createResponse.jsonPath().getString("bookingId");
        
        // Step 3: Wait for CONFIRMED
        ReportLogger.logStep("Step 3: Waiting for booking confirmation");
        String finalStatus = pollForTerminalStatus(bookingId, correlationId);
        assertThat(finalStatus).isEqualTo("CONFIRMED");
        
        // Step 4: Verify no loyalty accrual
        ReportLogger.logStep("Step 4: Verifying no loyalty accrual");
        Thread.sleep(1000);
        
        Response bookingResponse = bookingClient.get(BookingEndpoints.byId(bookingId), headers);
        String loyaltyAccrualStatus = bookingResponse.jsonPath().getString("loyaltyAccrualStatus");
        
        ReportLogger.info("Loyalty accrual status (no member): " + loyaltyAccrualStatus);
        
        // Should be NONE or null when no memberId provided
        assertThat(loyaltyAccrualStatus)
                .as("Loyalty accrual status should be NONE when no memberId")
                .isIn("NONE", null);
        
        ReportLogger.pass("✅ Correctly skipped loyalty accrual when no memberId provided");
    }

    private String pollForTerminalStatus(String bookingId, String correlationId) throws InterruptedException {
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        String status = "PENDING_PAYMENT";
        
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            Thread.sleep(POLL_INTERVAL_MS);
            
            Response statusResponse = bookingClient.get(BookingEndpoints.status(bookingId), headers);
            
            if (statusResponse.getStatusCode() == 200) {
                status = statusResponse.jsonPath().getString("status");
                ReportLogger.info("Poll attempt " + attempt + ": status = " + status);
                
                if (isTerminalStatus(status)) {
                    return status;
                }
            }
        }
        
        return status;
    }

    private boolean isTerminalStatus(String status) {
        return "CONFIRMED".equals(status) || "REJECTED".equals(status);
    }
}
