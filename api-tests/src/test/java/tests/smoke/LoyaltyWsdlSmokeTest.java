package tests.smoke;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.LoyaltyEndpoints;
import framework.reporting.ReportLogger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class LoyaltyWsdlSmokeTest {
    private ApiClient client;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String baseUrl = TestConfig.getInstance().getBaseUrl(ServiceType.LOYALTY);
        client = new RestAssuredApiClient(baseUrl);
    }

    @Test(groups = "smoke")
    public void wsdlShouldReturn200() {
        ReportLogger.logStep("Calling Loyalty Service WSDL endpoint");
        var response = client.get(LoyaltyEndpoints.WSDL, Collections.emptyMap());

        int actualStatus = response.getStatusCode();
        int expectedStatus = 200;
        boolean passed = actualStatus == expectedStatus;

        ReportLogger.logAssertion("Status code should be 200", expectedStatus, actualStatus, passed);

        assertThat(actualStatus)
                .as("GET /ws/loyalty.wsdl should return 200")
                .isEqualTo(expectedStatus);

        String body = response.getBody().asString();
        boolean containsDefinitions = body.contains("wsdl:definitions") || body.contains("definitions");
        ReportLogger.logAssertion("Response contains WSDL definitions", true, containsDefinitions, containsDefinitions);

        assertThat(body)
                .as("WSDL should contain definitions")
                .containsAnyOf("wsdl:definitions", "definitions");
    }
}
