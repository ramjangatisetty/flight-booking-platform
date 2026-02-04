package framework.bdd;

import framework.config.ServiceType;
import framework.headers.CorrelationIdSupport;
import framework.headers.IdempotencyKeySupport;
import framework.reporting.ReportLogger;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonStepDefinitions {
    private final TestContext context;

    public CommonStepDefinitions(TestContext context) {
        this.context = context;
    }

    @Before
    public void setUp() {
        context.reset();
    }

    @Given("I am testing the {string} service")
    public void iAmTestingTheService(String serviceName) {
        ServiceType serviceType = switch (serviceName.toLowerCase()) {
            case "booking" -> ServiceType.BOOKING;
            case "inventory" -> ServiceType.INVENTORY;
            case "payment" -> ServiceType.PAYMENT;
            case "baggage" -> ServiceType.BAGGAGE;
            case "loyalty" -> ServiceType.LOYALTY;
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };
        context.setClient(serviceType);
        if (serviceType == ServiceType.LOYALTY) {
            context.setSoapClient(serviceType);
        }
        ReportLogger.logStep("Testing " + serviceName + " service");
    }

    @Given("I set header {string} to {string}")
    public void iSetHeaderTo(String headerName, String headerValue) {
        String resolvedValue = resolveVariable(headerValue);
        context.setHeader(headerName, resolvedValue);
        ReportLogger.info("Set header " + headerName + " = " + resolvedValue);
    }

    @Given("I ensure a correlation id header is present")
    public void iEnsureACorrelationIdHeaderIsPresent() {
        String correlationId = CorrelationIdSupport.generate();
        context.setHeader(CorrelationIdSupport.HEADER_NAME, correlationId);
        context.set("correlationId", correlationId);
        ReportLogger.info("Generated correlation ID: " + correlationId);
    }

    @Given("I set an idempotency key header")
    public void iSetAnIdempotencyKeyHeader() {
        String idempotencyKey = IdempotencyKeySupport.generate();
        context.setHeader(IdempotencyKeySupport.HEADER_NAME, idempotencyKey);
        context.set("idempotencyKey", idempotencyKey);
        ReportLogger.info("Generated idempotency key: " + idempotencyKey);
    }

    @When("I call {string} {string}")
    public void iCall(String method, String path) {
        String resolvedPath = resolvePathVariables(path);
        Map<String, String> headers = context.getHeaders();
        Response response = switch (method.toUpperCase()) {
            case "GET" -> context.getClient().get(resolvedPath, headers);
            case "DELETE" -> context.getClient().delete(resolvedPath, headers);
            default -> throw new IllegalArgumentException("Use 'I call METHOD PATH with JSON body' for " + method);
        };
        context.setLastResponse(response);
    }

    @When("I call {string} {string} with JSON body")
    public void iCallWithJsonBody(String method, String path) {
        String resolvedPath = resolvePathVariables(path);
        Map<String, String> headers = context.getHeaders();
        Object body = context.getLastRequestBody();
        Response response = switch (method.toUpperCase()) {
            case "POST" -> context.getClient().post(resolvedPath, headers, body);
            case "PUT" -> context.getClient().put(resolvedPath, headers, body);
            case "PATCH" -> context.getClient().patch(resolvedPath, headers, body);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
        context.setLastResponse(response);
    }

    @When("I call {string} {string} with empty body")
    public void iCallWithEmptyBody(String method, String path) {
        String resolvedPath = resolvePathVariables(path);
        Map<String, String> headers = context.getHeaders();
        Response response = switch (method.toUpperCase()) {
            case "POST" -> context.getClient().post(resolvedPath, headers, "");
            case "PUT" -> context.getClient().put(resolvedPath, headers, "");
            case "PATCH" -> context.getClient().patch(resolvedPath, headers, "");
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
        context.setLastResponse(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        Response response = context.getLastResponse();
        int actualStatus = response.getStatusCode();
        boolean passed = actualStatus == expectedStatus;
        ReportLogger.logAssertion("Response status code", expectedStatus, actualStatus, passed);
        assertThat(actualStatus)
                .as("Response status code")
                .isEqualTo(expectedStatus);
    }

    @And("I capture {string} as {string}")
    public void iCaptureAs(String jsonPath, String variableName) {
        Response response = context.getLastResponse();
        Object value = response.jsonPath().get(jsonPath);
        context.set(variableName, value);
        ReportLogger.info("Captured " + variableName + " = " + value);
    }

    @And("the response json {string} should equal {string}")
    public void theResponseJsonShouldEqual(String jsonPath, String expectedValue) {
        Response response = context.getLastResponse();
        String actualValue = response.jsonPath().getString(jsonPath);
        String resolvedExpected = resolveVariable(expectedValue);
        boolean passed = resolvedExpected.equals(actualValue);
        ReportLogger.logAssertion("JSON path " + jsonPath, resolvedExpected, actualValue, passed);
        assertThat(actualValue)
                .as("JSON path %s", jsonPath)
                .isEqualTo(resolvedExpected);
    }

    @And("the response should contain {string}")
    public void theResponseShouldContain(String expectedText) {
        String body = context.getLastResponse().getBody().asString();
        boolean passed = body.contains(expectedText);
        ReportLogger.logAssertion("Response contains text", expectedText, passed ? "found" : "not found", passed);
        assertThat(body)
                .as("Response body should contain")
                .contains(expectedText);
    }

    private String resolveVariable(String value) {
        if (value.startsWith("{") && value.endsWith("}")) {
            String varName = value.substring(1, value.length() - 1);
            String resolved = context.getString(varName);
            return resolved != null ? resolved : value;
        }
        return value;
    }

    private String resolvePathVariables(String path) {
        String resolved = path;
        while (resolved.contains("{")) {
            int start = resolved.indexOf("{");
            int end = resolved.indexOf("}", start);
            if (end == -1) break;
            String varName = resolved.substring(start + 1, end);
            String varValue = context.getString(varName);
            if (varValue != null) {
                resolved = resolved.substring(0, start) + varValue + resolved.substring(end + 1);
            } else {
                break;
            }
        }
        return resolved;
    }
}
