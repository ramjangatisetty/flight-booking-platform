package framework.testkit;

import java.util.HashMap;
import java.util.Map;

public class FailureConfig {
    private final Map<String, Object> config = new HashMap<>();

    private FailureConfig() {}

    public static FailureConfig timeout(int ms) {
        FailureConfig fc = new FailureConfig();
        fc.config.put("type", "timeout");
        fc.config.put("timeoutMs", ms);
        return fc;
    }

    public static FailureConfig error(int statusCode) {
        FailureConfig fc = new FailureConfig();
        fc.config.put("type", "error");
        fc.config.put("statusCode", statusCode);
        return fc;
    }

    public static FailureConfig latency(int ms) {
        FailureConfig fc = new FailureConfig();
        fc.config.put("type", "latency");
        fc.config.put("latencyMs", ms);
        return fc;
    }

    public static FailureConfig disabled() {
        FailureConfig fc = new FailureConfig();
        fc.config.put("type", "disabled");
        return fc;
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(config);
    }
}
