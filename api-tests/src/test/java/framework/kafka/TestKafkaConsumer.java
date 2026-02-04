package framework.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import framework.reporting.ReportLogger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestKafkaConsumer implements AutoCloseable {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final KafkaConsumer<String, String> consumer;
    private final ConcurrentLinkedQueue<ConsumedEvent> eventBuffer = new ConcurrentLinkedQueue<>();
    private final List<String> topics;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean closeRequested = new AtomicBoolean(false);
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    private Thread pollingThread;

    public TestKafkaConsumer(String topic) {
        this(List.of(topic));
    }

    public TestKafkaConsumer(List<String> topics) {
        this.topics = new ArrayList<>(topics);
        this.consumer = createConsumer();
        this.consumer.subscribe(topics);
        startPolling();
        ReportLogger.info("Kafka consumer subscribed to topics: " + topics);
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaTestConfig.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "api-test-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        return new KafkaConsumer<>(props);
    }

    private void startPolling() {
        pollingThread = new Thread(() -> {
            try {
                // Initial polls to ensure consumer is ready and partitions are assigned
                // This is critical - we need to wait for partition assignment before the test proceeds
                int maxWaitAttempts = 50; // 10 seconds max
                int attempts = 0;
                while (running.get() && attempts < maxWaitAttempts) {
                    consumer.poll(Duration.ofMillis(200));
                    if (!consumer.assignment().isEmpty()) {
                        ReportLogger.info("Kafka consumer partitions assigned: " + consumer.assignment());
                        break;
                    }
                    attempts++;
                }
                if (consumer.assignment().isEmpty()) {
                    ReportLogger.info("Warning: Kafka consumer has no partitions assigned after " + attempts + " attempts");
                }
                
                while (running.get()) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                        for (ConsumerRecord<String, String> record : records) {
                            ConsumedEvent event = parseEvent(record);
                            if (event != null) {
                                eventBuffer.add(event);
                                ReportLogger.info("Received event: topic=" + event.topic() + " type=" + event.eventType() + " correlationId=" + event.correlationId());
                            }
                        }
                    } catch (Exception e) {
                        if (running.get()) {
                            ReportLogger.info("Kafka polling error: " + e.getMessage());
                        }
                    }
                }
            } finally {
                // Close consumer on the polling thread to avoid ConcurrentModificationException
                try {
                    consumer.close(Duration.ofSeconds(5));
                    ReportLogger.info("Kafka consumer closed on polling thread");
                } catch (Exception e) {
                    ReportLogger.info("Error closing Kafka consumer: " + e.getMessage());
                }
                closeLatch.countDown();
            }
        }, "kafka-test-consumer");
        pollingThread.setDaemon(true);
        pollingThread.start();
        
        // Wait for consumer to be ready (partitions assigned) before returning
        try {
            Thread.sleep(2000); // Give consumer time to join group and get partitions
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ConsumedEvent parseEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode json = MAPPER.readTree(record.value());
            JsonNode meta = json.get("meta");
            
            String eventType = meta != null && meta.has("eventType") 
                    ? meta.get("eventType").asText() : null;
            String correlationId = meta != null && meta.has("correlationId") 
                    ? meta.get("correlationId").asText() : null;
            
            return new ConsumedEvent(
                    record.topic(),
                    record.key(),
                    record.value(),
                    json,
                    eventType,
                    correlationId,
                    record.timestamp()
            );
        } catch (Exception e) {
            ReportLogger.info("Failed to parse event: " + e.getMessage());
            return null;
        }
    }

    public void clearBuffer() {
        eventBuffer.clear();
        ReportLogger.info("Cleared Kafka event buffer");
    }

    public Optional<ConsumedEvent> waitForEvent(String correlationId, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        
        while (System.currentTimeMillis() < deadline) {
            for (ConsumedEvent event : eventBuffer) {
                if (correlationId.equals(event.correlationId())) {
                    ReportLogger.info("Found event with correlationId: " + correlationId);
                    return Optional.of(event);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        ReportLogger.info("Timeout waiting for event with correlationId: " + correlationId);
        return Optional.empty();
    }

    public Optional<ConsumedEvent> waitForEventOfType(String eventType, String correlationId, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        
        while (System.currentTimeMillis() < deadline) {
            for (ConsumedEvent event : eventBuffer) {
                if (eventType.equals(event.eventType()) && correlationId.equals(event.correlationId())) {
                    ReportLogger.info("Found event type=" + eventType + " correlationId=" + correlationId);
                    return Optional.of(event);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Debug: log all events in buffer to help diagnose issues
        ReportLogger.info("Timeout waiting for event type=" + eventType + " correlationId=" + correlationId);
        ReportLogger.info("Events in buffer (" + eventBuffer.size() + "):");
        for (ConsumedEvent event : eventBuffer) {
            ReportLogger.info("  - topic=" + event.topic() + " type=" + event.eventType() + " correlationId=" + event.correlationId());
        }
        return Optional.empty();
    }

    public List<ConsumedEvent> getAllEvents() {
        return new ArrayList<>(eventBuffer);
    }

    public List<ConsumedEvent> getEventsByCorrelationId(String correlationId) {
        return eventBuffer.stream()
                .filter(e -> correlationId.equals(e.correlationId()))
                .toList();
    }

    @Override
    public void close() {
        if (closeRequested.compareAndSet(false, true)) {
            running.set(false);
            if (pollingThread != null) {
                pollingThread.interrupt();
            }
            // Wait for the polling thread to close the consumer
            try {
                if (!closeLatch.await(10, TimeUnit.SECONDS)) {
                    ReportLogger.info("Timeout waiting for Kafka consumer to close");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                ReportLogger.info("Interrupted while waiting for Kafka consumer to close");
            }
        }
    }
}
