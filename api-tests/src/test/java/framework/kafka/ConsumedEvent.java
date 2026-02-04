package framework.kafka;

import com.fasterxml.jackson.databind.JsonNode;

public record ConsumedEvent(
        String topic,
        String key,
        String rawValue,
        JsonNode json,
        String eventType,
        String correlationId,
        long timestamp
) {
    public JsonNode getData() {
        return json != null ? json.get("data") : null;
    }
    
    public JsonNode getMeta() {
        return json != null ? json.get("meta") : null;
    }
    
    public String getEventId() {
        JsonNode meta = getMeta();
        return meta != null && meta.has("eventId") ? meta.get("eventId").asText() : null;
    }
    
    public String getProducer() {
        JsonNode meta = getMeta();
        return meta != null && meta.has("producer") ? meta.get("producer").asText() : null;
    }
    
    public int getEventVersion() {
        JsonNode meta = getMeta();
        return meta != null && meta.has("eventVersion") ? meta.get("eventVersion").asInt() : 0;
    }
    
    public String getOccurredAt() {
        JsonNode meta = getMeta();
        return meta != null && meta.has("occurredAt") ? meta.get("occurredAt").asText() : null;
    }
}
