package framework.testkit;

import framework.utils.EnvUtils;

public final class LocalTestGuard {
    private LocalTestGuard() {}

    public static void ensureLocal() {
        if (!isLocal()) {
            throw new IllegalStateException("This operation is only allowed in local environment");
        }
    }

    public static boolean isLocal() {
        return EnvUtils.isLocal();
    }
}
