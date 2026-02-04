package framework.endpoints;

/**
 * Endpoint constants for local testkit operations.
 */
public final class TestkitEndpoints {

    public static final String RESET = "/test/reset";
    public static final String FAILURES = "/test/failures";
    public static final String EVENTS = "/test/events";

    private TestkitEndpoints() {
        // Constants class
    }
}
