package framework.config;

import framework.utils.EnvUtils;

/**
 * Centralized test configuration reading from environment variables.
 * Singleton pattern for consistent configuration across all tests.
 */
public class TestConfig {
    private static TestConfig instance;

    private final String baseUrlBooking;
    private final String baseUrlInventory;
    private final String baseUrlPayment;
    private final String baseUrlBaggage;
    private final String baseUrlLoyalty;
    private final String kafkaBootstrapServers;
    private final String env;
    private final boolean logHttp;

    private TestConfig() {
        this.baseUrlBooking = EnvUtils.getEnv("BASE_URL_BOOKING", "http://localhost:8081");
        this.baseUrlInventory = EnvUtils.getEnv("BASE_URL_INVENTORY", "http://localhost:8082");
        this.baseUrlPayment = EnvUtils.getEnv("BASE_URL_PAYMENT", "http://localhost:8083");
        this.baseUrlLoyalty = EnvUtils.getEnv("BASE_URL_LOYALTY", "http://localhost:8084");
        this.baseUrlBaggage = EnvUtils.getEnv("BASE_URL_BAGGAGE", "http://localhost:8085");
        this.kafkaBootstrapServers = EnvUtils.getEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        this.env = EnvUtils.getEnv("ENV", "local");
        this.logHttp = Boolean.parseBoolean(EnvUtils.getEnv("LOG_HTTP", "false"));
    }

    public static synchronized TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
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

    public String getBaseUrlBooking() {
        return baseUrlBooking;
    }

    public String getBaseUrlInventory() {
        return baseUrlInventory;
    }

    public String getBaseUrlPayment() {
        return baseUrlPayment;
    }

    public String getBaseUrlBaggage() {
        return baseUrlBaggage;
    }

    public String getBaseUrlLoyalty() {
        return baseUrlLoyalty;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public String getEnv() {
        return env;
    }

    public boolean isLocal() {
        return "local".equalsIgnoreCase(env);
    }

    public boolean isLogHttpEnabled() {
        return logHttp;
    }
}
