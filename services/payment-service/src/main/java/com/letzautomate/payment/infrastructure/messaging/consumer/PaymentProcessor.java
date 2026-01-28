package com.letzautomate.payment.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.payment.infrastructure.messaging.event.EventEnvelope;
import com.letzautomate.payment.infrastructure.messaging.event.PaymentFailedEvent;
import com.letzautomate.payment.infrastructure.messaging.event.PaymentSucceededEvent;
import com.letzautomate.payment.infrastructure.messaging.producer.PaymentEventPublisher;
import com.letzautomate.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import com.letzautomate.payment.infrastructure.persistence.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentProcessor {

	private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);
	public static final String TOPIC_PAYMENT_REQUESTED_V1 = "payment.requested.v1";

	private final PaymentEventPublisher publisher;
	private final ObjectMapper objectMapper;
	private final PaymentTransactionRepository transactionRepository;

	public PaymentProcessor(
			PaymentEventPublisher publisher,
			ObjectMapper objectMapper,
			PaymentTransactionRepository transactionRepository
	) {
		this.publisher = publisher;
		this.objectMapper = objectMapper;
		this.transactionRepository = transactionRepository;
	}

	@KafkaListener(topics = TOPIC_PAYMENT_REQUESTED_V1)
	public void onPaymentRequested(EventEnvelope message) {
		if (message == null || message.data == null || message.meta == null) {
			log.warn("Received invalid payment.requested event: null message or missing data/meta");
			return;
		}

		UUID eventId = message.meta.eventId;
		if (eventId == null) {
			log.error("Received payment.requested event without eventId - cannot deduplicate");
			return;
		}

		String requestEventId = eventId.toString();

		// message.data will be a Map/LinkedHashMap after JSON deserialization
		PaymentRequestedPayload data = objectMapper.convertValue(message.data, PaymentRequestedPayload.class);

		// IDEMPOTENCY CHECK: Look for existing transaction by request_event_id
		Optional<PaymentTransactionEntity> existing = transactionRepository.findByRequestEventId(requestEventId);
		if (existing.isPresent()) {
			PaymentTransactionEntity txn = existing.get();
			log.info("Payment already processed - deduplicating. bookingId={}, paymentId={}, requestEventId={}, status={}",
					txn.getBookingId(), txn.getPaymentId(), requestEventId, txn.getStatus());
			// Do NOT emit event again - idempotency achieved
			return;
		}

		// NEW PAYMENT: Execute business logic
		log.info("Processing new payment request. bookingId={}, requestEventId={}, amount={}, currency={}",
				data.bookingId, requestEventId, data.amount, data.currency);

		// Deterministic enterprise-friendly rule for now:
		// - Only USD allowed in Phase 1
		// - Amount < 300 => succeed, else fail
		boolean currencyOk = "USD".equalsIgnoreCase(data.currency);
		boolean amountOk = data.amount != null && data.amount.compareTo(new BigDecimal("300.00")) < 0;

		String key = data.bookingId.toString();
		UUID paymentId = UUID.randomUUID();
		UUID correlationId = message.meta.correlationId;

		if (currencyOk && amountOk) {
			// SUCCEEDED path
			if (persistTransactionSafely(paymentId, data.bookingId, "SUCCEEDED", data.amount, data.currency,
					"DUMMY", correlationId, requestEventId)) {
				PaymentSucceededEvent e = new PaymentSucceededEvent();
				e.bookingId = data.bookingId;
				e.paymentId = paymentId;
				e.amount = data.amount;
				e.currency = data.currency;
				e.provider = "DUMMY";
				e.status = "SUCCEEDED";

				EventEnvelope<PaymentSucceededEvent> env =
						EventEnvelope.of("payment.succeeded", 1, correlationId, "payment-service", e);

				publisher.publishSucceeded(key, env);
				log.info("Payment succeeded. bookingId={}, paymentId={}, requestEventId={}",
						data.bookingId, paymentId, requestEventId);
			}
		} else {
			// FAILED path
			String reasonCode;
			String reasonMessage;
			if (!currencyOk) {
				reasonCode = "UNSUPPORTED_CURRENCY";
				reasonMessage = "Only USD is supported in Phase 1";
			} else {
				reasonCode = "INSUFFICIENT_FUNDS";
				reasonMessage = "Payment declined by provider";
			}

			if (persistTransactionSafely(paymentId, data.bookingId, "FAILED", data.amount, data.currency,
					"DUMMY", correlationId, requestEventId)) {
				PaymentFailedEvent e = new PaymentFailedEvent();
				e.bookingId = data.bookingId;
				e.paymentId = paymentId;
				e.amount = data.amount;
				e.currency = data.currency;
				e.provider = "DUMMY";
				e.status = "FAILED";
				e.reasonCode = reasonCode;
				e.reasonMessage = reasonMessage;

				EventEnvelope<PaymentFailedEvent> env =
						EventEnvelope.of("payment.failed", 1, correlationId, "payment-service", e);

				publisher.publishFailed(key, env);
				log.info("Payment failed. bookingId={}, paymentId={}, requestEventId={}, reason={}",
						data.bookingId, paymentId, requestEventId, reasonCode);
			}
		}
	}

	/**
	 * Persist payment transaction for idempotency tracking.
	 * Returns true if persisted successfully, false if duplicate detected.
	 */
	@Transactional
	public boolean persistTransactionSafely(UUID paymentId, UUID bookingId, String status,
	                                         BigDecimal amount, String currency, String provider,
	                                         UUID correlationId, String requestEventId) {
		try {
			PaymentTransactionEntity entity = new PaymentTransactionEntity();
			entity.setPaymentId(paymentId);
			entity.setBookingId(bookingId);
			entity.setStatus(status);
			entity.setAmount(amount);
			entity.setCurrency(currency);
			entity.setProvider(provider);
			entity.setCorrelationId(correlationId);
			entity.setRequestEventId(requestEventId);
			entity.setCreatedAt(Instant.now());

			transactionRepository.save(entity);
			return true;
		} catch (DataIntegrityViolationException ex) {
			// Race condition: another thread/instance already persisted this transaction
			log.warn("Duplicate payment detected via constraint violation - deduplicating. bookingId={}, requestEventId={}",
					bookingId, requestEventId);
			return false;
		}
	}

	/**
	 * Payload for payment.requested.v1
	 */
	public record PaymentRequestedPayload(
			UUID bookingId,
			UUID reservationId,
			String flightId,
			String seatClass,
			BigDecimal amount,
			String currency
	) {}
}
