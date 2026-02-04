package framework.utils;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for UUID generation and validation.
 */
public final class UuidUtils {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private UuidUtils() {
        // Utility class
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }
}
