package com.letzautomate.inventory.infrastructure.messaging.producer;

import com.letzautomate.inventory.infrastructure.messaging.event.EventEnvelope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

	public static final String TOPIC_INVENTORY_RESERVED_V1 = "inventory.reserved.v1";
	public static final String TOPIC_INVENTORY_REJECTED_V1 = "inventory.rejected.v1";
	public static final String TOPIC_INVENTORY_RELEASED_V1 = "inventory.released.v1";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishReserved(String key, EventEnvelope<?> env) {
		kafkaTemplate.send(TOPIC_INVENTORY_RESERVED_V1, key, env);
	}

	public void publishRejected(String key, EventEnvelope<?> env) {
		kafkaTemplate.send(TOPIC_INVENTORY_REJECTED_V1, key, env);
	}

	public void publishReleased(String key, EventEnvelope<?> env) {
		kafkaTemplate.send(TOPIC_INVENTORY_RELEASED_V1, key, env);
	}
}