package com.letzautomate.baggage.infrastructure.messaging.producer;

import com.letzautomate.baggage.infrastructure.messaging.event.BaggageCheckedInEvent;
import com.letzautomate.baggage.infrastructure.messaging.event.BaggageStatusUpdatedEvent;
import com.letzautomate.baggage.infrastructure.messaging.event.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BaggageEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(BaggageEventPublisher.class);
	private static final String TOPIC = "baggage.events";
	private static final String PRODUCER = "baggage-service";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public BaggageEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishBaggageCheckedIn(BaggageCheckedInEvent event, UUID correlationId) {
		EventEnvelope<BaggageCheckedInEvent> envelope = EventEnvelope.of(
				"baggage.checked_in.v1",
				1,
				correlationId,
				PRODUCER,
				event
		);

		kafkaTemplate.send(TOPIC, event.bagTag, envelope);
		log.info("Published baggage.checked_in.v1: bagTag={}, bookingId={}, correlationId={}", 
				event.bagTag, event.bookingId, correlationId);
	}

	public void publishBaggageStatusUpdated(BaggageStatusUpdatedEvent event, UUID correlationId) {
		EventEnvelope<BaggageStatusUpdatedEvent> envelope = EventEnvelope.of(
				"baggage.status_updated.v1",
				1,
				correlationId,
				PRODUCER,
				event
		);

		kafkaTemplate.send(TOPIC, event.bagTag, envelope);
		log.info("Published baggage.status_updated.v1: bagTag={}, newStatus={}, correlationId={}", 
				event.bagTag, event.newStatus, correlationId);
	}
}
