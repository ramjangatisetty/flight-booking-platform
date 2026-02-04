package framework.clients;

import framework.config.TestConfig;
import framework.reporting.ReportLogger;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * JSON REST API client implementation using RestAssured.
 * This is the ONLY location where RestAssured is directly used for JSON REST services.
 */
public class RestAssuredApiClient implements ApiClient {

    private final String baseUrl;
    private final boolean logHttp;

    public RestAssuredApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logHttp = TestConfig.getInstance().isLogHttpEnabled();
    }

    @Override
    public Response get(String path, Map<String, String> headers) {
        ReportLogger.logRequest("GET", baseUrl, path, headers, null);
        Response response = buildRequest(headers)
                .get(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response post(String path, Map<String, String> headers, Object body) {
        ReportLogger.logRequest("POST", baseUrl, path, headers, body);
        Response response = buildRequest(headers)
                .body(body != null ? body : "")
                .post(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response put(String path, Map<String, String> headers, Object body) {
        ReportLogger.logRequest("PUT", baseUrl, path, headers, body);
        Response response = buildRequest(headers)
                .body(body != null ? body : "")
                .put(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response patch(String path, Map<String, String> headers, Object body) {
        ReportLogger.logRequest("PATCH", baseUrl, path, headers, body);
        Response response = buildRequest(headers)
                .body(body != null ? body : "")
                .patch(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    @Override
    public Response delete(String path, Map<String, String> headers) {
        ReportLogger.logRequest("DELETE", baseUrl, path, headers, null);
        Response response = buildRequest(headers)
                .delete(baseUrl + path);
        ReportLogger.logResponse(response);
        return response;
    }

    private RequestSpecification buildRequest(Map<String, String> headers) {
        RequestSpecification spec = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (headers != null && !headers.isEmpty()) {
            spec.headers(headers);
        }

        if (logHttp) {
            spec.log().all();
        }

        return spec;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
