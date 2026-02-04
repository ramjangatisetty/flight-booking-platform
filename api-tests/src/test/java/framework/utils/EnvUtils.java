package framework.utils;

public final class EnvUtils {
    private EnvUtils() {}

    public static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    public static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable not set: " + name);
        }
        return value;
    }

    public static boolean isLocal() {
        return "local".equalsIgnoreCase(getEnv("ENV", "local"));
    }
}
