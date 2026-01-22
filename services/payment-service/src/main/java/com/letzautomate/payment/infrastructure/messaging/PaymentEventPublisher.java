package com.letzautomate.payment.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

	public static final String TOPIC_PAYMENT_SUCCEEDED_V1 = "payment.succeeded.v1";
	public static final String TOPIC_PAYMENT_FAILED_V1 = "payment.failed.v1";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishSucceeded(String bookingIdKey, EventEnvelope<PaymentSucceededEvent> envelope) {
		kafkaTemplate.send(TOPIC_PAYMENT_SUCCEEDED_V1, bookingIdKey, envelope);
	}

	public void publishFailed(String bookingIdKey, EventEnvelope<PaymentFailedEvent> envelope) {
		kafkaTemplate.send(TOPIC_PAYMENT_FAILED_V1, bookingIdKey, envelope);
	}
}
