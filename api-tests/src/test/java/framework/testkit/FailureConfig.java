package framework.testkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for fault injection in local testkit.
 */
public class FailureConfig {

    private final Map<String, Object> config;

    private FailureConfig(Map<String, Object> config) {
        this.config = config;
    }

    public static FailureConfig timeout(int ms) {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "timeout");
        config.put("timeoutMs", ms);
        return new FailureConfig(config);
    }

    public static FailureConfig error(int statusCode) {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "error");
        config.put("statusCode", statusCode);
        return new FailureConfig(config);
    }

    public static FailureConfig latency(int ms) {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "latency");
        config.put("latencyMs", ms);
        return new FailureConfig(config);
    }

    public static FailureConfig disabled() {
        Map<String, Object> config = new HashMap<>();
        config.put("type", "disabled");
        return new FailureConfig(config);
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(config);
    }
}
