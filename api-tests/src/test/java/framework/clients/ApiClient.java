package framework.clients;

import io.restassured.response.Response;
import java.util.Map;

public interface ApiClient {
    Response get(String path, Map<String, String> headers);
    Response post(String path, Map<String, String> headers, Object body);
    Response put(String path, Map<String, String> headers, Object body);
    Response patch(String path, Map<String, String> headers, Object body);
    Response delete(String path, Map<String, String> headers);
}
