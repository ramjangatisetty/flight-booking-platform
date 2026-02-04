package framework.config;

public class TestConfig {
    private static final TestConfig INSTANCE = new TestConfig();

    private final String baseUrlBooking;
    private final String baseUrlInventory;
    private final String baseUrlPayment;
    private final String baseUrlBaggage;
    private final String baseUrlLoyalty;
    private final String env;
    private final boolean logHttp;

    private TestConfig() {
        this.baseUrlBooking = getEnv("BASE_URL_BOOKING", "http://localhost:8081");
        this.baseUrlInventory = getEnv("BASE_URL_INVENTORY", "http://localhost:8082");
        this.baseUrlPayment = getEnv("BASE_URL_PAYMENT", "http://localhost:8083");
        this.baseUrlLoyalty = getEnv("BASE_URL_LOYALTY", "http://localhost:8084");
        this.baseUrlBaggage = getEnv("BASE_URL_BAGGAGE", "http://localhost:8085");
        this.env = getEnv("ENV", "local");
        this.logHttp = Boolean.parseBoolean(getEnv("LOG_HTTP", "true"));
    }

    public static TestConfig getInstance() {
        return INSTANCE;
    }

    public String getBaseUrl(ServiceType service) {
        return switch (service) {
            case BOOKING -> baseUrlBooking;
            case INVENTORY -> baseUrlInventory;
            case PAYMENT -> baseUrlPayment;
            case BAGGAGE -> baseUrlBaggage;
            case LOYALTY -> baseUrlLoyalty;
        };
    }

    public String getEnv() {
        return env;
    }

    public boolean isLogHttp() {
        return logHttp;
    }

    public boolean isLocal() {
        return "local".equalsIgnoreCase(env);
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
