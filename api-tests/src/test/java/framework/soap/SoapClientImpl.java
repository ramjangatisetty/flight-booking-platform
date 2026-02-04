package framework.soap;

import framework.config.TestConfig;
import framework.reporting.ReportLogger;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

public class SoapClientImpl implements SoapClient {
    private final String baseUrl;
    private final boolean logHttp;

    public SoapClientImpl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logHttp = TestConfig.getInstance().isLogHttp();
    }

    @Override
    public SoapResponse sendRequest(String soapAction, String envelope) {
        Map<String, String> headers = Map.of("SOAPAction", soapAction);
        ReportLogger.logRequest("POST", baseUrl, "/ws", headers, envelope);

        var spec = RestAssured.given()
                .contentType("text/xml; charset=utf-8")
                .header("SOAPAction", soapAction)
                .body(envelope);

        if (logHttp) {
            spec.log().all();
        }

        Response response = spec.post(baseUrl + "/ws");
        ReportLogger.logResponse(response);

        String body = response.getBody().asString();
        SoapFault fault = SoapResponseParser.extractFault(body);
        return new SoapResponse(response.getStatusCode(), body, fault);
    }
}
