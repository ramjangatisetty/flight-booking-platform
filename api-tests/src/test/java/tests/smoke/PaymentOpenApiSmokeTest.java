package tests.smoke;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.endpoints.PaymentEndpoints;
import framework.reporting.ReportLogger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test for Payment Service OpenAPI docs endpoint.
 */
public class PaymentOpenApiSmokeTest {

    private ApiClient client;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String baseUrl = TestConfig.getInstance().getBaseUrl(ServiceType.PAYMENT);
        client = new RestAssuredApiClient(baseUrl);
    }

    @Test(groups = "smoke")
    public void apiDocsShouldReturn200() {
        ReportLogger.logStep("Calling Payment Service OpenAPI docs endpoint");
        var response = client.get(PaymentEndpoints.API_DOCS, Collections.emptyMap());

        int actualStatus = response.getStatusCode();
        int expectedStatus = 200;
        boolean passed = actualStatus == expectedStatus;

        ReportLogger.logAssertion("Status code should be 200", expectedStatus, actualStatus, passed);

        assertThat(actualStatus)
                .as("GET /v3/api-docs should return 200")
                .isEqualTo(expectedStatus);
    }
}
