package framework.kafka;

import framework.utils.EnvUtils;

public final class KafkaTestConfig {
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    
    private KafkaTestConfig() {}
    
    public static String getBootstrapServers() {
        return EnvUtils.getEnv("KAFKA_BOOTSTRAP_SERVERS", DEFAULT_BOOTSTRAP_SERVERS);
    }
    
    public static int getDefaultTimeoutSeconds() {
        return Integer.parseInt(EnvUtils.getEnv("KAFKA_TIMEOUT_SECONDS", "10"));
    }
    
    public static int getSagaTimeoutSeconds() {
        return Integer.parseInt(EnvUtils.getEnv("KAFKA_SAGA_TIMEOUT_SECONDS", "30"));
    }
}
