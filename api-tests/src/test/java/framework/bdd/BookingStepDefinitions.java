package framework.bdd;

import framework.models.request.CreateBookingRequest;
import framework.reporting.ReportLogger;
import io.cucumber.java.en.Given;

import java.math.BigDecimal;

public class BookingStepDefinitions {
    private final TestContext context;

    public BookingStepDefinitions(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid CreateBookingRequest JSON payload")
    public void iHaveAValidCreateBookingRequestPayload() {
        CreateBookingRequest request = new CreateBookingRequest(
                "FL" + System.currentTimeMillis(),
                "ECONOMY",
                new BigDecimal("299.99"),
                "USD",
                null
        );
        context.setLastRequestBody(request);
        ReportLogger.info("Created valid CreateBookingRequest payload");
    }

    @Given("I have a CreateBookingRequest with flightId {string} and seatClass {string}")
    public void iHaveACreateBookingRequestWithFlightIdAndSeatClass(String flightId, String seatClass) {
        CreateBookingRequest request = new CreateBookingRequest(
                flightId,
                seatClass,
                new BigDecimal("299.99"),
                "USD",
                null
        );
        context.setLastRequestBody(request);
        ReportLogger.info("Created CreateBookingRequest with flightId=" + flightId + ", seatClass=" + seatClass);
    }

    @Given("I have a CreateBookingRequest with memberId {string}")
    public void iHaveACreateBookingRequestWithMemberId(String memberId) {
        String resolvedMemberId = memberId.startsWith("{") && memberId.endsWith("}")
                ? context.getString(memberId.substring(1, memberId.length() - 1))
                : memberId;
        CreateBookingRequest request = new CreateBookingRequest(
                "FL" + System.currentTimeMillis(),
                "ECONOMY",
                new BigDecimal("299.99"),
                "USD",
                resolvedMemberId
        );
        context.setLastRequestBody(request);
        ReportLogger.info("Created CreateBookingRequest with memberId=" + resolvedMemberId);
    }
}
