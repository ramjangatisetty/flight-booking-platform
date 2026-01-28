package com.letzautomate.payment.infrastructure.messaging.producer;

import com.letzautomate.payment.infrastructure.messaging.event.EventEnvelope;
import com.letzautomate.payment.infrastructure.messaging.event.PaymentFailedEvent;
import com.letzautomate.payment.infrastructure.messaging.event.PaymentSucceededEvent;
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

	/** Publish payment.succeeded.v1 */
	public void publishSucceeded(String key, EventEnvelope<PaymentSucceededEvent> envelope) {
		kafkaTemplate.send(TOPIC_PAYMENT_SUCCEEDED_V1, key, envelope);
	}

	/** Publish payment.failed.v1 */
	public void publishFailed(String key, EventEnvelope<PaymentFailedEvent> envelope) {
		kafkaTemplate.send(TOPIC_PAYMENT_FAILED_V1, key, envelope);
	}

	/**
	 * Keep this convenience method if you still want it elsewhere.
	 * Not required by PaymentProcessor (since it already builds payload+envelope).
	 */
	public void publishPaymentFailed(
			java.util.UUID bookingId,
			java.util.UUID correlationId,
			java.util.UUID paymentId,
			String provider,
			String reasonCode,
			String reasonMessage
	) {
		PaymentFailedEvent payload = new PaymentFailedEvent();
		payload.bookingId = bookingId;
		payload.paymentId = paymentId;
		payload.provider = provider;
		payload.status = "FAILED";
		payload.reasonCode = reasonCode;
		payload.reasonMessage = reasonMessage;

		EventEnvelope<PaymentFailedEvent> envelope =
				EventEnvelope.of("payment.failed", 1, correlationId, "payment-service", payload);

		kafkaTemplate.send(TOPIC_PAYMENT_FAILED_V1, bookingId.toString(), envelope);
	}
}