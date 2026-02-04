package framework.utils;

import java.util.UUID;
import java.util.regex.Pattern;

public final class UuidUtils {
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private UuidUtils() {}

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }
}
