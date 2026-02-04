package framework.testkit;

import framework.clients.ApiClient;
import framework.endpoints.TestkitEndpoints;
import framework.utils.JsonUtils;
import io.restassured.response.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for local testkit operations (reset, fault injection, event inspection).
 * Only available when ENV=local.
 */
public class LocalTestClient {

    private final ApiClient client;

    public LocalTestClient(ApiClient client) {
        LocalTestGuard.ensureLocal();
        this.client = client;
    }

    public void reset() {
        LocalTestGuard.ensureLocal();
        client.post(TestkitEndpoints.RESET, Collections.emptyMap(), null);
    }

    public void configureFailures(Map<String, Object> config) {
        LocalTestGuard.ensureLocal();
        client.post(TestkitEndpoints.FAILURES, Collections.emptyMap(), config);
    }

    public void configureFailures(FailureConfig config) {
        configureFailures(config.toMap());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> events() {
        LocalTestGuard.ensureLocal();
        Response response = client.get(TestkitEndpoints.EVENTS, Collections.emptyMap());
        if (response.getStatusCode() == 200) {
            return JsonUtils.fromJson(response.getBody().asString(), List.class);
        }
        return Collections.emptyList();
    }
}
