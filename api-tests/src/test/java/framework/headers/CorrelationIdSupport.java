package framework.headers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CorrelationIdSupport {
    public static final String HEADER_NAME = "X-Correlation-Id";

    private CorrelationIdSupport() {}

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static Map<String, String> withCorrelationId(Map<String, String> headers) {
        return withCorrelationId(headers, generate());
    }

    public static Map<String, String> withCorrelationId(Map<String, String> headers, String correlationId) {
        Map<String, String> result = new HashMap<>(headers != null ? headers : Map.of());
        result.put(HEADER_NAME, correlationId);
        return result;
    }
}
