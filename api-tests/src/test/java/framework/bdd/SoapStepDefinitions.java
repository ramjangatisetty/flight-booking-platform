package framework.bdd;

import framework.endpoints.LoyaltyEndpoints;
import framework.reporting.ReportLogger;
import framework.soap.LoyaltySoapRequestBuilder;
import framework.soap.SoapResponse;
import framework.soap.SoapResponseParser;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SoapStepDefinitions {
    private final TestContext context;
    private final Map<String, String> soapElements = new HashMap<>();
    private String currentOperation;

    public SoapStepDefinitions(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid EnrollMemberRequest SOAP request")
    public void iHaveAValidEnrollMemberRequestSoapRequest() {
        currentOperation = "EnrollMember";
        soapElements.clear();
        // Use unique email to avoid "already exists" errors
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";
        soapElements.put("firstName", "John");
        soapElements.put("lastName", "Doe");
        soapElements.put("email", uniqueEmail);
        ReportLogger.info("Prepared EnrollMemberRequest SOAP request with email: " + uniqueEmail);
    }

    @Given("I have a valid GetMemberStatusRequest SOAP request")
    public void iHaveAValidGetMemberStatusRequestSoapRequest() {
        currentOperation = "GetMemberStatus";
        soapElements.clear();
        ReportLogger.info("Prepared GetMemberStatusRequest SOAP request");
    }

    @Given("I have a valid AccruePointsRequest SOAP request")
    public void iHaveAValidAccruePointsRequestSoapRequest() {
        currentOperation = "AccruePoints";
        soapElements.clear();
        ReportLogger.info("Prepared AccruePointsRequest SOAP request");
    }

    @Given("I set SOAP element {string} to {string}")
    public void iSetSoapElementTo(String elementName, String value) {
        String resolvedValue = resolveVariable(value);
        soapElements.put(elementName, resolvedValue);
        ReportLogger.info("Set SOAP element " + elementName + " = " + resolvedValue);
    }

    @When("I call SOAP operation {string}")
    public void iCallSoapOperation(String operation) {
        String envelope = buildSoapEnvelope(operation);
        String soapAction = LoyaltyEndpoints.getSoapAction(operation);
        SoapResponse response = context.getSoapClient().sendRequest(soapAction, envelope);
        context.setLastSoapResponse(response);
        ReportLogger.logStep("Called SOAP operation: " + operation);
    }

    @Then("the SOAP response should be successful")
    public void theSoapResponseShouldBeSuccessful() {
        SoapResponse response = context.getLastSoapResponse();
        boolean passed = response.getStatusCode() == 200 && !response.isFault();
        ReportLogger.logAssertion("SOAP response successful", "200 OK, no fault",
                response.getStatusCode() + (response.isFault() ? " with fault" : " OK"), passed);
        assertThat(response.getStatusCode())
                .as("SOAP response status")
                .isEqualTo(200);
        assertThat(response.isFault())
                .as("SOAP response should not be a fault")
                .isFalse();
    }

    @Then("the SOAP response should be a fault")
    public void theSoapResponseShouldBeAFault() {
        SoapResponse response = context.getLastSoapResponse();
        boolean passed = response.isFault();
        ReportLogger.logAssertion("SOAP response is fault", true, response.isFault(), passed);
        assertThat(response.isFault())
                .as("SOAP response should be a fault")
                .isTrue();
    }

    @And("I capture SOAP element {string} as {string}")
    public void iCaptureSoapElementAs(String elementName, String variableName) {
        SoapResponse response = context.getLastSoapResponse();
        String value = SoapResponseParser.extractElement(response.getRawResponse(), elementName);
        context.set(variableName, value);
        ReportLogger.info("Captured SOAP element " + variableName + " = " + value);
    }

    @And("the SOAP response element {string} should equal {string}")
    public void theSoapResponseElementShouldEqual(String elementName, String expectedValue) {
        SoapResponse response = context.getLastSoapResponse();
        String actualValue = SoapResponseParser.extractElement(response.getRawResponse(), elementName);
        String resolvedExpected = resolveVariable(expectedValue);
        boolean passed = resolvedExpected.equals(actualValue);
        ReportLogger.logAssertion("SOAP element " + elementName, resolvedExpected, actualValue, passed);
        assertThat(actualValue)
                .as("SOAP element %s", elementName)
                .isEqualTo(resolvedExpected);
    }

    @And("the SOAP fault code should be {string}")
    public void theSoapFaultCodeShouldBe(String expectedCode) {
        SoapResponse response = context.getLastSoapResponse();
        String actualCode = response.getFault().getFaultCode();
        boolean passed = expectedCode.equals(actualCode);
        ReportLogger.logAssertion("SOAP fault code", expectedCode, actualCode, passed);
        assertThat(actualCode)
                .as("SOAP fault code")
                .isEqualTo(expectedCode);
    }

    @And("the SOAP fault message should contain {string}")
    public void theSoapFaultMessageShouldContain(String expectedText) {
        SoapResponse response = context.getLastSoapResponse();
        String faultString = response.getFault().getFaultString();
        boolean passed = faultString != null && faultString.contains(expectedText);
        ReportLogger.logAssertion("SOAP fault message contains", expectedText,
                faultString != null ? faultString : "null", passed);
        assertThat(faultString)
                .as("SOAP fault message")
                .contains(expectedText);
    }

    private String buildSoapEnvelope(String operation) {
        return switch (operation) {
            case "EnrollMember" -> LoyaltySoapRequestBuilder.enrollMember(
                    soapElements.getOrDefault("firstName", "John"),
                    soapElements.getOrDefault("lastName", "Doe"),
                    soapElements.getOrDefault("email", "test@example.com")
            );
            case "GetMemberStatus" -> LoyaltySoapRequestBuilder.getMemberStatus(
                    soapElements.get("memberId")
            );
            case "AccruePoints" -> LoyaltySoapRequestBuilder.accruePoints(
                    soapElements.get("memberId"),
                    soapElements.get("bookingId"),
                    soapElements.getOrDefault("amount", "100.00"),
                    soapElements.getOrDefault("currency", "USD"),
                    soapElements.getOrDefault("correlationId", java.util.UUID.randomUUID().toString())
            );
            default -> throw new IllegalArgumentException("Unknown SOAP operation: " + operation);
        };
    }

    private String resolveVariable(String value) {
        if (value.startsWith("{") && value.endsWith("}")) {
            String varName = value.substring(1, value.length() - 1);
            String resolved = context.getString(varName);
            return resolved != null ? resolved : value;
        }
        return value;
    }
}
