package framework.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import framework.kafka.ConsumedEvent;
import framework.kafka.TestKafkaConsumer;
import framework.reporting.ReportLogger;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaStepDefinitions {
    private final TestContext context;
    private TestKafkaConsumer kafkaConsumer;
    private ConsumedEvent lastEvent;

    public KafkaStepDefinitions(TestContext context) {
        this.context = context;
    }

    @After("@kafka")
    public void closeKafkaConsumer() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
            kafkaConsumer = null;
        }
    }

    @Given("I am subscribed to Kafka topic {string}")
    public void iAmSubscribedToKafkaTopic(String topic) {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
        kafkaConsumer = new TestKafkaConsumer(topic);
        kafkaConsumer.clearBuffer();
        ReportLogger.info("Subscribed to Kafka topic: " + topic);
    }

    @Given("I am subscribed to Kafka topics:")
    public void iAmSubscribedToKafkaTopics(DataTable dataTable) {
        List<String> topics = dataTable.asList();
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
        kafkaConsumer = new TestKafkaConsumer(topics);
        kafkaConsumer.clearBuffer();
        ReportLogger.info("Subscribed to Kafka topics: " + topics);
    }

    @Given("I clear the Kafka event buffer")
    public void iClearTheKafkaEventBuffer() {
        if (kafkaConsumer != null) {
            kafkaConsumer.clearBuffer();
        }
        ReportLogger.info("Cleared Kafka event buffer");
    }

    @Then("I should receive a Kafka event with type {string} within {int} seconds")
    public void iShouldReceiveAKafkaEventWithTypeWithinSeconds(String eventType, int seconds) {
        assertThat(kafkaConsumer).as("Kafka consumer must be initialized").isNotNull();
        
        String correlationId = context.getString("correlationId");
        assertThat(correlationId).as("Correlation ID must be set").isNotNull();
        
        Optional<ConsumedEvent> event = kafkaConsumer.waitForEventOfType(
                eventType, correlationId, Duration.ofSeconds(seconds));
        
        boolean found = event.isPresent();
        ReportLogger.logAssertion("Kafka event received", eventType, 
                found ? event.get().eventType() : "not found", found);
        
        assertThat(event)
                .as("Should receive Kafka event with type '%s' and correlationId '%s'", eventType, correlationId)
                .isPresent();
        
        lastEvent = event.get();
        context.set("lastKafkaEvent", lastEvent);
    }

    @Then("I should receive a Kafka event within {int} seconds")
    public void iShouldReceiveAKafkaEventWithinSeconds(int seconds) {
        assertThat(kafkaConsumer).as("Kafka consumer must be initialized").isNotNull();
        
        String correlationId = context.getString("correlationId");
        assertThat(correlationId).as("Correlation ID must be set").isNotNull();
        
        Optional<ConsumedEvent> event = kafkaConsumer.waitForEvent(
                correlationId, Duration.ofSeconds(seconds));
        
        boolean found = event.isPresent();
        ReportLogger.logAssertion("Kafka event received", "any event", 
                found ? event.get().eventType() : "not found", found);
        
        assertThat(event)
                .as("Should receive Kafka event with correlationId '%s'", correlationId)
                .isPresent();
        
        lastEvent = event.get();
        context.set("lastKafkaEvent", lastEvent);
    }

    @Then("the event correlationId should match the request correlationId")
    public void theEventCorrelationIdShouldMatchTheRequestCorrelationId() {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        String expectedCorrelationId = context.getString("correlationId");
        String actualCorrelationId = lastEvent.correlationId();
        
        boolean passed = expectedCorrelationId.equals(actualCorrelationId);
        ReportLogger.logAssertion("Event correlationId", expectedCorrelationId, actualCorrelationId, passed);
        
        assertThat(actualCorrelationId)
                .as("Event correlationId should match request")
                .isEqualTo(expectedCorrelationId);
    }

    @Then("the event data should contain field {string}")
    public void theEventDataShouldContainField(String fieldName) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        JsonNode data = lastEvent.getData();
        assertThat(data).as("Event should have data section").isNotNull();
        
        boolean hasField = data.has(fieldName);
        ReportLogger.logAssertion("Event data contains field", fieldName, 
                hasField ? "present" : "missing", hasField);
        
        assertThat(hasField)
                .as("Event data should contain field '%s'", fieldName)
                .isTrue();
    }

    @Then("the event meta should contain field {string}")
    public void theEventMetaShouldContainField(String fieldName) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        JsonNode meta = lastEvent.getMeta();
        assertThat(meta).as("Event should have meta section").isNotNull();
        
        boolean hasField = meta.has(fieldName);
        ReportLogger.logAssertion("Event meta contains field", fieldName, 
                hasField ? "present" : "missing", hasField);
        
        assertThat(hasField)
                .as("Event meta should contain field '%s'", fieldName)
                .isTrue();
    }

    @Then("the event meta {string} should equal {string}")
    public void theEventMetaShouldEqual(String fieldName, String expectedValue) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        JsonNode meta = lastEvent.getMeta();
        assertThat(meta).as("Event should have meta section").isNotNull();
        assertThat(meta.has(fieldName)).as("Meta should have field '%s'", fieldName).isTrue();
        
        String actualValue = meta.get(fieldName).asText();
        boolean passed = expectedValue.equals(actualValue);
        ReportLogger.logAssertion("Event meta." + fieldName, expectedValue, actualValue, passed);
        
        assertThat(actualValue)
                .as("Event meta.%s", fieldName)
                .isEqualTo(expectedValue);
    }

    @Then("the event data {string} should equal {string}")
    public void theEventDataShouldEqual(String fieldName, String expectedValue) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        JsonNode data = lastEvent.getData();
        assertThat(data).as("Event should have data section").isNotNull();
        assertThat(data.has(fieldName)).as("Data should have field '%s'", fieldName).isTrue();
        
        String resolvedExpected = resolveVariable(expectedValue);
        String actualValue = data.get(fieldName).asText();
        boolean passed = resolvedExpected.equals(actualValue);
        ReportLogger.logAssertion("Event data." + fieldName, resolvedExpected, actualValue, passed);
        
        assertThat(actualValue)
                .as("Event data.%s", fieldName)
                .isEqualTo(resolvedExpected);
    }

    @And("I capture event data {string} as {string}")
    public void iCaptureEventDataAs(String fieldName, String variableName) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        JsonNode data = lastEvent.getData();
        assertThat(data).as("Event should have data section").isNotNull();
        assertThat(data.has(fieldName)).as("Data should have field '%s'", fieldName).isTrue();
        
        String value = data.get(fieldName).asText();
        context.set(variableName, value);
        ReportLogger.info("Captured event data " + variableName + " = " + value);
    }

    @And("I capture event meta {string} as {string}")
    public void iCaptureEventMetaAs(String fieldName, String variableName) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        JsonNode meta = lastEvent.getMeta();
        assertThat(meta).as("Event should have meta section").isNotNull();
        assertThat(meta.has(fieldName)).as("Meta should have field '%s'", fieldName).isTrue();
        
        String value = meta.get(fieldName).asText();
        context.set(variableName, value);
        ReportLogger.info("Captured event meta " + variableName + " = " + value);
    }

    @Then("the event should be on topic {string}")
    public void theEventShouldBeOnTopic(String expectedTopic) {
        assertThat(lastEvent).as("Must receive an event first").isNotNull();
        
        String actualTopic = lastEvent.topic();
        boolean passed = expectedTopic.equals(actualTopic);
        ReportLogger.logAssertion("Event topic", expectedTopic, actualTopic, passed);
        
        assertThat(actualTopic)
                .as("Event should be on topic")
                .isEqualTo(expectedTopic);
    }

    private String resolveVariable(String value) {
        if (value.startsWith("{") && value.endsWith("}")) {
            String varName = value.substring(1, value.length() - 1);
            String resolved = context.getString(varName);
            return resolved != null ? resolved : value;
        }
        return value;
    }
}
