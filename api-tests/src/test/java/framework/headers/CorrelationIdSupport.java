package framework.headers;

import framework.utils.UuidUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for managing X-Correlation-Id headers.
 */
public final class CorrelationIdSupport {

    public static final String HEADER_NAME = "X-Correlation-Id";

    private CorrelationIdSupport() {
        // Utility class
    }

    public static String generate() {
        return UuidUtils.generate();
    }

    public static Map<String, String> withCorrelationId(Map<String, String> headers) {
        return withCorrelationId(headers, generate());
    }

    public static Map<String, String> withCorrelationId(Map<String, String> headers, String correlationId) {
        Map<String, String> result = new HashMap<>();
        if (headers != null) {
            result.putAll(headers);
        }
        result.put(HEADER_NAME, correlationId);
        return result;
    }
}
