package tests.e2e;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.BookingEndpoints;
import framework.endpoints.InventoryEndpoints;
import framework.headers.CorrelationIdSupport;
import framework.reporting.ReportLogger;
import framework.requests.BookingRequests;
import framework.requests.InventoryRequests;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end saga flow tests for the booking workflow.
 * 
 * Tests the complete saga: Booking → Inventory → Payment → Confirmed
 * 
 * Prerequisites:
 * - All services running (booking:8081, inventory:8082, payment:8083)
 * - Kafka running for event propagation
 * - PostgreSQL databases available
 */
public class BookingSagaE2ETest {

    private ApiClient bookingClient;
    private ApiClient inventoryClient;
    
    private static final String TEST_FLIGHT_ID = "E2E-FL001";
    private static final int POLL_INTERVAL_MS = 500;
    private static final int MAX_POLL_ATTEMPTS = 20; // 10 seconds max

    @BeforeClass(alwaysRun = true)
    public void setup() {
        TestConfig config = TestConfig.getInstance();
        bookingClient = new RestAssuredApiClient(config.getBaseUrl(ServiceType.BOOKING));
        inventoryClient = new RestAssuredApiClient(config.getBaseUrl(ServiceType.INVENTORY));
    }

    @Test(groups = {"e2e", "saga"})
    public void shouldCompleteBookingSagaSuccessfully() throws InterruptedException {
        ReportLogger.logStep("=== E2E SAGA TEST: Happy Path ===");
        
        // Step 1: Seed inventory
        ReportLogger.logStep("Step 1: Seeding inventory for flight " + TEST_FLIGHT_ID);
        Map<String, Object> seedRequest = InventoryRequests.seedInventory(TEST_FLIGHT_ID, "ECONOMY", 10);
        Response seedResponse = inventoryClient.post(InventoryEndpoints.ADMIN_SEED, new HashMap<>(), seedRequest);
        
        ReportLogger.logAssertion("Inventory seed should succeed", 200, seedResponse.getStatusCode(), 
                seedResponse.getStatusCode() == 200);
        assertThat(seedResponse.getStatusCode())
                .as("Inventory seeding should return 200")
                .isEqualTo(200);

        // Step 2: Create booking
        ReportLogger.logStep("Step 2: Creating booking for flight " + TEST_FLIGHT_ID);
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> bookingRequest = BookingRequests.withFlightId(TEST_FLIGHT_ID);
        
        Response createResponse = bookingClient.post(BookingEndpoints.BASE, headers, bookingRequest);
        
        ReportLogger.logAssertion("Booking creation should return 201", 201, createResponse.getStatusCode(),
                createResponse.getStatusCode() == 201);
        assertThat(createResponse.getStatusCode())
                .as("Booking creation should return 201 Created")
                .isEqualTo(201);

        String bookingId = createResponse.jsonPath().getString("bookingId");
        String initialStatus = createResponse.jsonPath().getString("status");
        
        ReportLogger.info("Created booking: " + bookingId + " with initial status: " + initialStatus);
        assertThat(bookingId).as("BookingId should be present").isNotNull();
        assertThat(initialStatus).as("Initial status should be PENDING_PAYMENT").isEqualTo("PENDING_PAYMENT");

        // Step 3: Poll for CONFIRMED status
        ReportLogger.logStep("Step 3: Polling for booking confirmation (max " + (MAX_POLL_ATTEMPTS * POLL_INTERVAL_MS / 1000) + "s)");
        
        String finalStatus = pollForTerminalStatus(bookingId, correlationId);
        
        ReportLogger.logAssertion("Final status should be CONFIRMED", "CONFIRMED", finalStatus,
                "CONFIRMED".equals(finalStatus));
        assertThat(finalStatus)
                .as("Booking should reach CONFIRMED status after saga completes")
                .isEqualTo("CONFIRMED");
        
        ReportLogger.pass("✅ E2E Saga completed successfully: PENDING_PAYMENT → CONFIRMED");
    }

    @Test(groups = {"e2e", "saga"})
    public void shouldFailBookingWhenNoInventory() throws InterruptedException {
        ReportLogger.logStep("=== E2E SAGA TEST: No Inventory ===");
        
        String noInventoryFlightId = "E2E-NOINV-" + System.currentTimeMillis();
        
        // Step 1: Create booking for flight with no inventory
        ReportLogger.logStep("Step 1: Creating booking for flight with no inventory: " + noInventoryFlightId);
        String correlationId = CorrelationIdSupport.generate();
        Map<String, String> headers = CorrelationIdSupport.withCorrelationId(new HashMap<>(), correlationId);
        Map<String, Object> bookingRequest = BookingRequests.withFlightId(noInventoryFlightId);
        
        Response createResponse = bookingClient.post(BookingEndpoints.BASE, headers, bookingRequest);
        
        assertThat(createResponse.getStatusCode())
                .as("Booking creation should return 201 Created")
                .isEqualTo(201);

        String bookingId = createResponse.jsonPath().getString("bookingId");
        ReportLogger.info("Created booking: " + bookingId);

        // Step 2: Poll for REJECTED status (inventory rejection)
        ReportLogger.logStep("Step 2: Polling for booking rejection due to no inventory");
        
        String finalStatus = pollForTerminalStatus(bookingId, correlationId);
        
        ReportLogger.logAssertion("Final status should be REJECTED", "REJECTED", finalStatus,
                "REJECTED".equals(finalStatus));
        assertThat(finalStatus)
                .as("Booking should reach REJECTED status when no inventory available")
                .isEqualTo("REJECTED");
        
        ReportLogger.pass("✅ E2E Saga correctly rejected: PENDING_PAYMENT → REJECTED (no inventory)");
    }

    /**
     * Polls the booking status endpoint until a terminal status is reached or timeout.
     */
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
            } else {
                ReportLogger.info("Poll attempt " + attempt + ": HTTP " + statusResponse.getStatusCode());
            }
        }
        
        ReportLogger.fail("Timeout waiting for terminal status. Last status: " + status);
        return status;
    }

    private boolean isTerminalStatus(String status) {
        return "CONFIRMED".equals(status) || "REJECTED".equals(status);
    }
}
