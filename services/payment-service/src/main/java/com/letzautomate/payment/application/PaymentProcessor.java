package com.letzautomate.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.payment.infrastructure.messaging.*;
import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessor {

	public static final String TOPIC_BOOKING_CREATED_V1 = "booking.created.v1";

	private final PaymentEventPublisher publisher;
	private final ObjectMapper objectMapper;

	public PaymentProcessor(PaymentEventPublisher publisher, ObjectMapper objectMapper) {
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = TOPIC_BOOKING_CREATED_V1)
	public void onBookingCreated(EventEnvelope message) {
		if (message == null || message.data == null || message.meta == null) return;

		// message.data will be a Map/LinkedHashMap after JSON deserialization
		BookingCreatedEvent data = objectMapper.convertValue(message.data, BookingCreatedEvent.class);

		// Deterministic enterprise-friendly rule for now:
		// - Only USD allowed in Phase 1
		// - Amount < 300 => succeed, else fail
		boolean currencyOk = "USD".equalsIgnoreCase(data.currency);
		boolean amountOk = data.amount != null && data.amount.compareTo(new BigDecimal("300.00")) < 0;

		String key = data.bookingId.toString();
		UUID paymentId = UUID.randomUUID();

		if (currencyOk && amountOk) {
			PaymentSucceededEvent e = new PaymentSucceededEvent();
			e.bookingId = data.bookingId;
			e.paymentId = paymentId;
			e.amount = data.amount;
			e.currency = data.currency;
			e.provider = "DUMMY";
			e.status = "SUCCEEDED";

			EventEnvelope<PaymentSucceededEvent> env =
					EventEnvelope.of("payment.succeeded", 1, message.meta.correlationId, "payment-service", e);

			publisher.publishSucceeded(key, env);
		} else {
			PaymentFailedEvent e = new PaymentFailedEvent();
			e.bookingId = data.bookingId;
			e.paymentId = paymentId;
			e.amount = data.amount;
			e.currency = data.currency;
			e.provider = "DUMMY";
			e.status = "FAILED";

			if (!currencyOk) {
				e.reasonCode = "UNSUPPORTED_CURRENCY";
				e.reasonMessage = "Only USD is supported in Phase 1";
			} else {
				e.reasonCode = "INSUFFICIENT_FUNDS";
				e.reasonMessage = "Payment declined by provider";
			}

			EventEnvelope<PaymentFailedEvent> env =
					EventEnvelope.of("payment.failed", 1, message.meta.correlationId, "payment-service", e);

			publisher.publishFailed(key, env);
		}
	}
}
