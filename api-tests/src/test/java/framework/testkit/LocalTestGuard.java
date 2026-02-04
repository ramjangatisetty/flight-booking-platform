package framework.testkit;

import framework.utils.EnvUtils;

/**
 * Guard utility to ensure testkit operations only run in local environment.
 */
public final class LocalTestGuard {

    private LocalTestGuard() {
        // Utility class
    }

    public static void ensureLocal() {
        if (!isLocal()) {
            throw new IllegalStateException(
                    "Testkit operations are only allowed in local environment. " +
                            "Current ENV: " + EnvUtils.getEnv("ENV", "local")
            );
        }
    }

    public static boolean isLocal() {
        return EnvUtils.isLocal();
    }
}
