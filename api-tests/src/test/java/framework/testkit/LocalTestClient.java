package framework.testkit;

import framework.clients.ApiClient;
import framework.endpoints.TestkitEndpoints;
import framework.utils.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LocalTestClient {
    private final ApiClient client;

    public LocalTestClient(ApiClient client) {
        LocalTestGuard.ensureLocal();
        this.client = client;
    }

    public void reset() {
        client.post(TestkitEndpoints.RESET, Collections.emptyMap(), null);
    }

    public void configureFailures(Map<String, Object> config) {
        client.post(TestkitEndpoints.FAILURES, Collections.emptyMap(), config);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> events() {
        var response = client.get(TestkitEndpoints.EVENTS, Collections.emptyMap());
        return JsonUtils.fromJson(response.getBody().asString(), List.class);
    }
}
