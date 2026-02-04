package framework.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import framework.config.TestConfig;
import framework.reporting.ReportLogger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Test Kafka consumer for verifying event publication.
 * Used in black-box API tests to verify events are published correctly.
 * 
 * Event Envelope Structure (from booking-service):
 * {
 *   "meta": {
 *     "eventId": "uuid",
 *     "eventType": "inventory.reserve.requested",
 *     "eventVersion": 1,
 *     "occurredAt": "2024-01-01T00:00:00Z",
 *     "correlationId": "uuid",
 *     "producer": "booking-service"
 *   },
 *   "data": { ... payload ... }
 * }
 */
public class TestKafkaConsumer implements AutoCloseable {

    private final KafkaConsumer<String, String> consumer;
    private final ObjectMapper objectMapper;
    private final Queue<ConsumedEvent> eventBuffer;
    private final List<String> topics;

    /**
     * Creates a consumer for a single topic.
     */
    public TestKafkaConsumer(String topic) {
        this(Collections.singletonList(topic));
    }

    /**
     * Creates a consumer for multiple topics.
     */
    public TestKafkaConsumer(List<String> topics) {
        this.topics = new ArrayList<>(topics);
        this.objectMapper = new ObjectMapper();
        this.eventBuffer = new ConcurrentLinkedQueue<>();
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConfig.getInstance().getKafkaBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "api-test-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Use "earliest" to ensure we don't miss events published right after subscription
        // Each test run uses a unique group ID, so we'll read from the beginning of the topic
        // but only events published during this test session will match our correlation IDs
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        // Reduce session timeout for faster consumer group rebalancing
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(topics);
        
        // Multiple polls to ensure consumer is fully joined to the group
        // and has received partition assignments
        for (int i = 0; i < 5; i++) {
            consumer.poll(Duration.ofMillis(200));
        }
        
        ReportLogger.info("Kafka test consumer subscribed to topics: " + topics);
    }

    /**
     * Polls for events and returns any matching the correlation ID.
     */
    public Optional<ConsumedEvent> waitForEvent(String correlationId, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        
        while (System.currentTimeMillis() < deadline) {
            // Check buffer first
            Optional<ConsumedEvent> buffered = findInBuffer(correlationId);
            if (buffered.isPresent()) {
                return buffered;
            }
            
            // Poll for new events
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            
            for (ConsumerRecord<String, String> record : records) {
                try {
                    JsonNode json = objectMapper.readTree(record.value());
                    String eventCorrelationId = extractCorrelationId(json);
                    String eventType = extractEventType(json);
                    
                    ConsumedEvent event = new ConsumedEvent(
                            record.topic(),
                            record.key(),
                            record.value(),
                            json,
                            eventType,
                            eventCorrelationId,
                            record.timestamp()
                    );
                    
                    eventBuffer.add(event);
                    ReportLogger.info("Consumed event from topic " + record.topic() + ": " + eventType);
                    
                    if (correlationId.equals(eventCorrelationId)) {
                        ReportLogger.info("Found matching event: " + eventType + " with correlationId: " + correlationId);
                        return Optional.of(event);
                    }
                } catch (Exception e) {
                    ReportLogger.info("Failed to parse event: " + e.getMessage());
                }
            }
        }
        
        ReportLogger.info("Timeout waiting for event with correlationId: " + correlationId);
        return Optional.empty();
    }

    /**
     * Waits for an event of a specific type with the given correlation ID.
     */
    public Optional<ConsumedEvent> waitForEventOfType(String eventType, String correlationId, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        
        while (System.currentTimeMillis() < deadline) {
            // Check buffer first for matching event type
            Optional<ConsumedEvent> buffered = eventBuffer.stream()
                    .filter(e -> correlationId.equals(e.correlationId()) && eventType.equals(e.eventType()))
                    .findFirst();
            if (buffered.isPresent()) {
                return buffered;
            }
            
            Optional<ConsumedEvent> event = waitForEvent(correlationId, Duration.ofMillis(500));
            if (event.isPresent() && eventType.equals(event.get().eventType())) {
                return event;
            }
        }
        
        return Optional.empty();
    }

    private Optional<ConsumedEvent> findInBuffer(String correlationId) {
        return eventBuffer.stream()
                .filter(e -> correlationId.equals(e.correlationId()))
                .findFirst();
    }

    /**
     * Extracts correlationId from the event envelope.
     * Structure: { "meta": { "correlationId": "uuid" }, "data": {...} }
     */
    private String extractCorrelationId(JsonNode json) {
        // New envelope structure: meta.correlationId
        if (json.has("meta") && json.get("meta").has("correlationId")) {
            return json.get("meta").get("correlationId").asText();
        }
        // Fallback for legacy structure
        if (json.has("correlationId")) {
            return json.get("correlationId").asText();
        }
        return null;
    }

    /**
     * Extracts eventType from the event envelope.
     * Structure: { "meta": { "eventType": "inventory.reserve.requested" }, "data": {...} }
     */
    private String extractEventType(JsonNode json) {
        // New envelope structure: meta.eventType
        if (json.has("meta") && json.get("meta").has("eventType")) {
            return json.get("meta").get("eventType").asText();
        }
        // Fallback for legacy structure
        if (json.has("eventType")) {
            return json.get("eventType").asText();
        }
        return "unknown";
    }

    public void clearBuffer() {
        eventBuffer.clear();
    }

    @Override
    public void close() {
        if (consumer != null) {
            consumer.close();
            ReportLogger.info("Kafka test consumer closed");
        }
    }

    /**
     * Record representing a consumed Kafka event.
     * 
     * Event Envelope Structure:
     * - meta.eventId: Unique event identifier
     * - meta.eventType: Event type (e.g., "inventory.reserve.requested")
     * - meta.eventVersion: Version number
     * - meta.occurredAt: Timestamp
     * - meta.correlationId: Correlation ID for tracing
     * - meta.producer: Service that produced the event
     * - data: Event payload
     */
    public record ConsumedEvent(
            String topic,
            String key,
            String rawValue,
            JsonNode json,
            String eventType,
            String correlationId,
            long timestamp
    ) {
        /**
         * Gets the event payload (data field).
         */
        public JsonNode getData() {
            return json.has("data") ? json.get("data") : json;
        }

        /**
         * Gets the meta section of the event.
         */
        public JsonNode getMeta() {
            return json.has("meta") ? json.get("meta") : null;
        }

        /**
         * Gets the eventId from meta.
         */
        public String getEventId() {
            JsonNode meta = getMeta();
            return meta != null && meta.has("eventId") ? meta.get("eventId").asText() : null;
        }

        /**
         * Gets the producer from meta.
         */
        public String getProducer() {
            JsonNode meta = getMeta();
            return meta != null && meta.has("producer") ? meta.get("producer").asText() : null;
        }

        /**
         * @deprecated Use getData() instead - matches actual event structure
         */
        @Deprecated
        public JsonNode getPayload() {
            return getData();
        }
    }
}
