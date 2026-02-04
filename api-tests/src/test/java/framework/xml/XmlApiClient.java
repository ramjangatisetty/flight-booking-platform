package framework.xml;

import framework.clients.ApiClient;
import framework.config.TestConfig;
import framework.reporting.ReportLogger;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class XmlApiClient implements ApiClient {
    private final String baseUrl;
    private final boolean logHttp;

    public XmlApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logHttp = TestConfig.getInstance().isLogHttp();
    }

    @Override
    public Response get(String path, Map<String, String> headers) {
        ReportLogger.logRequest("GET", baseUrl, path, headers, null);
        Response response = buildRequest(headers).get(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response post(String path, Map<String, String> headers, Object body) {
        ReportLogger.logRequest("POST", baseUrl, path, headers, body);
        Response response = buildRequest(headers).body(body).post(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response put(String path, Map<String, String> headers, Object body) {
        ReportLogger.logRequest("PUT", baseUrl, path, headers, body);
        Response response = buildRequest(headers).body(body).put(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response patch(String path, Map<String, String> headers, Object body) {
        ReportLogger.logRequest("PATCH", baseUrl, path, headers, body);
        Response response = buildRequest(headers).body(body).patch(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response delete(String path, Map<String, String> headers) {
        ReportLogger.logRequest("DELETE", baseUrl, path, headers, null);
        Response response = buildRequest(headers).delete(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    private RequestSpecification buildRequest(Map<String, String> headers) {
        RequestSpecification spec = RestAssured.given()
                .contentType("application/xml")
                .accept("application/xml");
        if (headers != null) {
            headers.forEach(spec::header);
        }
        if (logHttp) {
            spec.log().all();
        }
        return spec;
    }
}
