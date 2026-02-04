package tests.smoke;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.BaggageEndpoints;
import framework.reporting.ReportLogger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class BaggageOpenApiSmokeTest {
    private ApiClient client;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String baseUrl = TestConfig.getInstance().getBaseUrl(ServiceType.BAGGAGE);
        client = new RestAssuredApiClient(baseUrl);
    }

    @Test(groups = "smoke")
    public void healthEndpointShouldReturn200() {
        ReportLogger.logStep("Calling Baggage Service health endpoint");
        var response = client.get(BaggageEndpoints.HEALTH, Collections.emptyMap());

        int actualStatus = response.getStatusCode();
        int expectedStatus = 200;
        boolean passed = actualStatus == expectedStatus;

        ReportLogger.logAssertion("Status code should be 200", expectedStatus, actualStatus, passed);

        assertThat(actualStatus)
                .as("GET /actuator/health should return 200")
                .isEqualTo(expectedStatus);
    }
}
