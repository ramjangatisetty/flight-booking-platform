package framework.headers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IdempotencyKeySupport {
    public static final String HEADER_NAME = "Idempotency-Key";

    private IdempotencyKeySupport() {}

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static Map<String, String> withIdempotencyKey(Map<String, String> headers) {
        return withIdempotencyKey(headers, generate());
    }

    public static Map<String, String> withIdempotencyKey(Map<String, String> headers, String key) {
        Map<String, String> result = new HashMap<>(headers != null ? headers : Map.of());
        result.put(HEADER_NAME, key);
        return result;
    }
}
