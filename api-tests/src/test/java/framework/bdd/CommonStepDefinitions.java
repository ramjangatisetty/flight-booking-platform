package framework.bdd;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.headers.CorrelationIdSupport;
import framework.headers.IdempotencyKeySupport;
import framework.soap.SoapClientImpl;
import framework.xml.XmlApiClient;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common step definitions implementing canonical step vocabulary.
 */
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
        ServiceType serviceType = ServiceType.valueOf(serviceName.toUpperCase());
        String baseUrl = TestConfig.getInstance().getBaseUrl(serviceType);

        if (serviceType == ServiceType.BAGGAGE) {
            context.setClient(new XmlApiClient(baseUrl));
        } else if (serviceType == ServiceType.LOYALTY) {
            context.setClient(new RestAssuredApiClient(baseUrl));
            context.setSoapClient(new SoapClientImpl(baseUrl));
        } else {
            context.setClient(new RestAssuredApiClient(baseUrl));
        }
    }

    @Given("I set header {string} to {string}")
    public void iSetHeaderTo(String name, String value) {
        context.setHeader(name, value);
    }

    @Given("I ensure a correlation id header is present")
    public void iEnsureACorrelationIdHeaderIsPresent() {
        if (!context.getHeaders().containsKey(CorrelationIdSupport.HEADER_NAME)) {
            context.setHeader(CorrelationIdSupport.HEADER_NAME, CorrelationIdSupport.generate());
        }
    }

    @Given("I set an idempotency key header")
    public void iSetAnIdempotencyKeyHeader() {
        context.setHeader(IdempotencyKeySupport.HEADER_NAME, IdempotencyKeySupport.generate());
    }

    @When("I call {string} {string}")
    public void iCall(String method, String path) {
        ApiClient client = context.getClient();
        Response response;

        // Replace path variables with stored values
        path = replacePlaceholders(path);

        switch (method.toUpperCase()) {
            case "GET" -> response = client.get(path, context.getHeaders());
            case "POST" -> response = client.post(path, context.getHeaders(), context.getLastRequestBody());
            case "PUT" -> response = client.put(path, context.getHeaders(), context.getLastRequestBody());
            case "PATCH" -> response = client.patch(path, context.getHeaders(), context.getLastRequestBody());
            case "DELETE" -> response = client.delete(path, context.getHeaders());
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        context.setLastResponse(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        Response response = context.getLastResponse();
        assertThat(response.getStatusCode())
                .as("Response status code")
                .isEqualTo(expectedStatus);
    }

    @And("I capture {string} as {string}")
    public void iCaptureAs(String jsonPath, String variableName) {
        Response response = context.getLastResponse();
        JsonPath jp = response.jsonPath();
        Object value = jp.get(jsonPath);
        context.set(variableName, value);
    }

    @And("the response json {string} should equal {string}")
    public void theResponseJsonShouldEqual(String jsonPath, String expectedValue) {
        Response response = context.getLastResponse();
        JsonPath jp = response.jsonPath();

        // Replace placeholders in expected value
        expectedValue = replacePlaceholders(expectedValue);

        String actualValue = jp.getString(jsonPath);
        assertThat(actualValue)
                .as("JSON path " + jsonPath)
                .isEqualTo(expectedValue);
    }

    private String replacePlaceholders(String text) {
        if (text == null) {
            return null;
        }
        // Replace {variableName} with stored values
        for (String key : text.split("\\{")) {
            if (key.contains("}")) {
                String varName = key.substring(0, key.indexOf("}"));
                Object value = context.get(varName);
                if (value != null) {
                    text = text.replace("{" + varName + "}", value.toString());
                }
            }
        }
        return text;
    }
}
