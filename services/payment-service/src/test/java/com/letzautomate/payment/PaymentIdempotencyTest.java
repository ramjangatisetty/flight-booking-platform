package com.letzautomate.payment;

import com.letzautomate.payment.infrastructure.messaging.consumer.PaymentProcessor;
import com.letzautomate.payment.infrastructure.messaging.event.EventEnvelope;
import com.letzautomate.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import com.letzautomate.payment.infrastructure.persistence.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test payment service idempotency:
 * - Processing the same payment.requested event twice should result in only one DB row
 * - Only one payment.succeeded/failed event should be published (verified by DB count)
 */
@SpringBootTest
@ActiveProfiles("local")
class PaymentIdempotencyTest {

	@Autowired
	private PaymentProcessor paymentProcessor;

	@Autowired
	private PaymentTransactionRepository transactionRepository;

	@Test
	void shouldDeduplicatePaymentRequest_whenProcessedTwice() {
		// Given: A payment.requested event
		UUID bookingId = UUID.randomUUID();
		UUID eventId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();

		EventEnvelope<Map<String, Object>> event = createPaymentRequestedEvent(
				bookingId, eventId, correlationId, new BigDecimal("150.00"), "USD"
		);

		// When: Process the same event twice
		paymentProcessor.onPaymentRequested(event);
		paymentProcessor.onPaymentRequested(event); // Duplicate

		// Then: Only one transaction should exist in DB
		List<PaymentTransactionEntity> transactions = transactionRepository.findAll();
		long countForBooking = transactions.stream()
				.filter(t -> t.getBookingId().equals(bookingId))
				.count();

		assertEquals(1, countForBooking, "Should have exactly one transaction for the booking");

		// Verify the transaction details
		PaymentTransactionEntity txn = transactionRepository.findByBookingId(bookingId).orElseThrow();
		assertEquals("SUCCEEDED", txn.getStatus());
		assertEquals(new BigDecimal("150.00"), txn.getAmount());
		assertEquals("USD", txn.getCurrency());
		assertEquals(eventId.toString(), txn.getRequestEventId());
		assertEquals(correlationId, txn.getCorrelationId());
	}

	@Test
	void shouldDeduplicateFailedPayment_whenProcessedTwice() {
		// Given: A payment.requested event that will fail (amount >= 300)
		UUID bookingId = UUID.randomUUID();
		UUID eventId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();

		EventEnvelope<Map<String, Object>> event = createPaymentRequestedEvent(
				bookingId, eventId, correlationId, new BigDecimal("350.00"), "USD"
		);

		// When: Process the same event twice
		paymentProcessor.onPaymentRequested(event);
		paymentProcessor.onPaymentRequested(event); // Duplicate

		// Then: Only one transaction should exist in DB
		List<PaymentTransactionEntity> transactions = transactionRepository.findAll();
		long countForBooking = transactions.stream()
				.filter(t -> t.getBookingId().equals(bookingId))
				.count();

		assertEquals(1, countForBooking, "Should have exactly one transaction for the booking");

		// Verify the transaction details
		PaymentTransactionEntity txn = transactionRepository.findByBookingId(bookingId).orElseThrow();
		assertEquals("FAILED", txn.getStatus());
		assertEquals(new BigDecimal("350.00"), txn.getAmount());
		assertEquals("USD", txn.getCurrency());
		assertEquals(eventId.toString(), txn.getRequestEventId());
	}

	@Test
	void shouldHandleConstraintViolation_whenDifferentEventForSameBooking() {
		// Given: Two different payment.requested events for the same booking
		// The booking_id unique constraint will prevent the second one
		UUID bookingId = UUID.randomUUID();
		UUID eventId1 = UUID.randomUUID();
		UUID eventId2 = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();

		EventEnvelope<Map<String, Object>> event1 = createPaymentRequestedEvent(
				bookingId, eventId1, correlationId, new BigDecimal("100.00"), "USD"
		);

		// When: Process first event
		paymentProcessor.onPaymentRequested(event1);

		// Then: First transaction exists
		assertEquals(1, transactionRepository.findAll().stream()
				.filter(t -> t.getBookingId().equals(bookingId))
				.count());

		// When: Try to process second event with different eventId but same bookingId
		// The constraint violation is caught inside the @Transactional method
		EventEnvelope<Map<String, Object>> event2 = createPaymentRequestedEvent(
				bookingId, eventId2, correlationId, new BigDecimal("100.00"), "USD"
		);

		// This should not throw - the exception is caught and logged
		assertDoesNotThrow(() -> paymentProcessor.onPaymentRequested(event2));

		// Then: Still only one transaction (first one wins)
		assertEquals(1, transactionRepository.findAll().stream()
				.filter(t -> t.getBookingId().equals(bookingId))
				.count());

		// Verify it's the first transaction that persisted
		PaymentTransactionEntity txn = transactionRepository.findByBookingId(bookingId).orElseThrow();
		assertEquals(eventId1.toString(), txn.getRequestEventId());
	}

	private EventEnvelope<Map<String, Object>> createPaymentRequestedEvent(
			UUID bookingId, UUID eventId, UUID correlationId, BigDecimal amount, String currency
	) {
		EventEnvelope<Map<String, Object>> envelope = new EventEnvelope<>();

		EventEnvelope.Meta meta = new EventEnvelope.Meta();
		meta.eventId = eventId;
		meta.eventType = "payment.requested";
		meta.eventVersion = 1;
		meta.occurredAt = Instant.now();
		meta.correlationId = correlationId;
		meta.producer = "inventory-service";

		Map<String, Object> data = new HashMap<>();
		data.put("bookingId", bookingId);
		data.put("reservationId", UUID.randomUUID());
		data.put("flightId", "TEST123");
		data.put("seatClass", "ECONOMY");
		data.put("amount", amount);
		data.put("currency", currency);

		envelope.meta = meta;
		envelope.data = data;

		return envelope;
	}
}
