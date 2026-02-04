package framework.bdd;

import framework.models.request.SeedInventoryRequest;
import framework.reporting.ReportLogger;
import io.cucumber.java.en.Given;

public class InventoryStepDefinitions {
    private final TestContext context;

    public InventoryStepDefinitions(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid SeedInventoryRequest JSON payload")
    public void iHaveAValidSeedInventoryRequestPayload() {
        SeedInventoryRequest request = new SeedInventoryRequest(
                "FL" + System.currentTimeMillis(),
                "ECONOMY",
                100
        );
        context.setLastRequestBody(request);
        ReportLogger.info("Created valid SeedInventoryRequest payload");
    }

    @Given("I have a SeedInventoryRequest with flightId {string} and seatClass {string} and availableSeats {int}")
    public void iHaveASeedInventoryRequestWithValues(String flightId, String seatClass, int availableSeats) {
        SeedInventoryRequest request = new SeedInventoryRequest(
                flightId,
                seatClass,
                availableSeats
        );
        context.setLastRequestBody(request);
        ReportLogger.info("Created SeedInventoryRequest with flightId=" + flightId + 
                ", seatClass=" + seatClass + ", availableSeats=" + availableSeats);
    }
}
