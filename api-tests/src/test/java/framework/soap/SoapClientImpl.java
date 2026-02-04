package framework.soap;

import framework.config.TestConfig;
import framework.reporting.ReportLogger;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * SOAP client implementation using RestAssured.
 */
public class SoapClientImpl implements SoapClient {

    private static final String CONTENT_TYPE_SOAP = "text/xml; charset=utf-8";
    private static final String SOAP_ENDPOINT = "/ws";

    private final String baseUrl;
    private final boolean logHttp;

    public SoapClientImpl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logHttp = TestConfig.getInstance().isLogHttpEnabled();
    }

    @Override
    public SoapResponse sendRequest(String soapAction, String envelope) {
        Map<String, String> headers = Map.of("SOAPAction", soapAction);
        ReportLogger.logRequest("POST", baseUrl, SOAP_ENDPOINT, headers, envelope);

        RequestSpecification spec = RestAssured.given()
                .contentType(CONTENT_TYPE_SOAP)
                .header("SOAPAction", soapAction)
                .body(envelope);

        if (logHttp) {
            spec.log().all();
        }

        Response response = spec.post(baseUrl + SOAP_ENDPOINT);
        ReportLogger.logResponse(response);

        return new SoapResponse(response.getStatusCode(), response.getBody().asString());
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
