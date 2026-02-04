package framework.headers;

import framework.utils.UuidUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for managing Idempotency-Key headers.
 */
public final class IdempotencyKeySupport {

    public static final String HEADER_NAME = "Idempotency-Key";

    private IdempotencyKeySupport() {
        // Utility class
    }

    public static String generate() {
        return UuidUtils.generate();
    }

    public static Map<String, String> withIdempotencyKey(Map<String, String> headers) {
        return withIdempotencyKey(headers, generate());
    }

    public static Map<String, String> withIdempotencyKey(Map<String, String> headers, String key) {
        Map<String, String> result = new HashMap<>();
        if (headers != null) {
            result.putAll(headers);
        }
        result.put(HEADER_NAME, key);
        return result;
    }
}
