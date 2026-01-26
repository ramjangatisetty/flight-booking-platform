package com.letzautomate.inventory.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.inventory.infrastructure.messaging.EventEnvelope;
import com.letzautomate.inventory.infrastructure.messaging.InventoryEventPublisher;
import com.letzautomate.inventory.infrastructure.messaging.InventoryRejectedEvent;
import com.letzautomate.inventory.infrastructure.messaging.InventoryReservedEvent;
import com.letzautomate.inventory.infrastructure.messaging.PaymentFailedEvent;
import com.letzautomate.inventory.infrastructure.messaging.PaymentSucceededEvent;
import com.letzautomate.inventory.infrastructure.persistence.BookingDetailsEntity;
import com.letzautomate.inventory.infrastructure.persistence.BookingDetailsRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryProcessor {

	public static final String TOPIC_BOOKING_CREATED_V1 = "booking.created.v1";
	public static final String TOPIC_PAYMENT_SUCCEEDED_V1 = "payment.succeeded.v1";
	public static final String TOPIC_PAYMENT_FAILED_V1 = "payment.failed.v1";

	private final InventoryService inventoryService;
	private final InventoryEventPublisher publisher;
	private final BookingDetailsRepository bookingDetailsRepo;
	private final ObjectMapper objectMapper;

	// Demo-friendly: wait briefly for booking snapshot if payment arrives first
	private static final int SNAPSHOT_MAX_ATTEMPTS = 10; // 10 * 200ms = ~2s
	private static final long SNAPSHOT_SLEEP_MS = 200L;

	public InventoryProcessor(
			InventoryService inventoryService,
			InventoryEventPublisher publisher,
			BookingDetailsRepository bookingDetailsRepo,
			ObjectMapper objectMapper
	) {
		this.inventoryService = inventoryService;
		this.publisher = publisher;
		this.bookingDetailsRepo = bookingDetailsRepo;
		this.objectMapper = objectMapper;
	}

	/**
	 * Step 1: Consume booking.created and store bookingId -> flightId, seatClass in H2.
	 */
	@KafkaListener(
			topics = TOPIC_BOOKING_CREATED_V1,
			containerFactory = "paymentSucceededListenerFactory"
	)
	public void onBookingCreated(EventEnvelope<?> message) {
		if (message == null || message.data == null) return;

		BookingCreatedPayload payload = objectMapper.convertValue(message.data, BookingCreatedPayload.class);
		if (payload == null || payload.bookingId() == null) return;

		bookingDetailsRepo.findById(payload.bookingId())
				.map(existing -> {
					existing.setFlightId(payload.flightId());
					existing.setSeatClass(payload.seatClass());
					return bookingDetailsRepo.save(existing);
				})
				.orElseGet(() -> {
					BookingDetailsEntity e = new BookingDetailsEntity();
					e.setBookingId(payload.bookingId());
					e.setFlightId(payload.flightId());
					e.setSeatClass(payload.seatClass());
					return bookingDetailsRepo.save(e);
				});
	}

	/**
	 * Step 2: Consume payment.succeeded, lookup booking snapshot from H2, reserve, publish inventory event.
	 * If snapshot is missing (out-of-order across topics), wait briefly and retry.
	 */
	@KafkaListener(
			topics = TOPIC_PAYMENT_SUCCEEDED_V1,
			containerFactory = "paymentSucceededListenerFactory"
	)
	public void onPaymentSucceeded(EventEnvelope<?> message) {
		if (message == null || message.data == null) return;

		PaymentSucceededEvent payment = objectMapper.convertValue(message.data, PaymentSucceededEvent.class);
		if (payment == null || payment.bookingId == null) return;

		String key = payment.bookingId.toString();
		UUID correlationId = safeCorrelationId(message);

		Optional<BookingDetailsEntity> bookingDetailsOpt = waitForBookingSnapshot(payment.bookingId);
		if (bookingDetailsOpt.isEmpty()) {
			publishMissingSnapshotReject(key, correlationId, payment.bookingId);
			return;
		}

		var bookingDetails = bookingDetailsOpt.get();

		var result = inventoryService.reserve(
				payment.bookingId,
				bookingDetails.getFlightId(),
				bookingDetails.getSeatClass()
		);

		if ("RESERVED".equalsIgnoreCase(result.status())) {
			var reservedEnv = EventEnvelope.of(
					"inventory.reserved",
					1,
					correlationId,
					"inventory-service",
					InventoryReservedEvent.of(payment.bookingId, result.reservationId())
			);
			publisher.publishReserved(key, reservedEnv);
		} else {
			InventoryRejectedEvent rej = new InventoryRejectedEvent();
			rej.bookingId = payment.bookingId;
			rej.status = "REJECTED";
			rej.reason = (result.reason() == null || result.reason().isBlank()) ? "NO_SEATS" : result.reason();

			var rejectedEnv = EventEnvelope.of(
					"inventory.rejected",
					1,
					correlationId,
					"inventory-service",
					rej
			);
			publisher.publishRejected(key, rejectedEnv);
		}
	}

	/**
	 * Payment failure flow:
	 * Consume payment.failed and publish inventory.rejected with the payment reasonCode.
	 * Inventory should NOT decrement seats here.
	 */
	@KafkaListener(
			topics = TOPIC_PAYMENT_FAILED_V1,
			containerFactory = "paymentSucceededListenerFactory"
	)
	public void onPaymentFailed(EventEnvelope<?> message) {
		if (message == null || message.data == null) return;

		PaymentFailedEvent payment = objectMapper.convertValue(message.data, PaymentFailedEvent.class);
		if (payment == null || payment.bookingId == null) return;

		String key = payment.bookingId.toString();
		UUID correlationId = safeCorrelationId(message);

		Optional<BookingDetailsEntity> bookingDetailsOpt = waitForBookingSnapshot(payment.bookingId);
		if (bookingDetailsOpt.isEmpty()) {
			publishMissingSnapshotReject(key, correlationId, payment.bookingId);
			return;
		}

		var bookingDetails = bookingDetailsOpt.get();

		String reason = (payment.reasonCode == null || payment.reasonCode.isBlank())
				? "PAYMENT_FAILED"
				: payment.reasonCode;

		// Persist a REJECTED reservation with reason (no seat decrement)
		inventoryService.reject(
				payment.bookingId,
				bookingDetails.getFlightId(),
				bookingDetails.getSeatClass(),
				reason
		);

		InventoryRejectedEvent rej = new InventoryRejectedEvent();
		rej.bookingId = payment.bookingId;
		rej.status = "REJECTED";
		rej.reason = reason;

		var rejectedEnv = EventEnvelope.of(
				"inventory.rejected",
				1,
				correlationId,
				"inventory-service",
				rej
		);
		publisher.publishRejected(key, rejectedEnv);
	}

	private void publishMissingSnapshotReject(String key, UUID correlationId, UUID bookingId) {
		InventoryRejectedEvent rej = new InventoryRejectedEvent();
		rej.bookingId = bookingId;
		rej.status = "REJECTED";
		rej.reason = "MISSING_BOOKING_SNAPSHOT";

		var rejectedEnv = EventEnvelope.of(
				"inventory.rejected",
				1,
				correlationId,
				"inventory-service",
				rej
		);
		publisher.publishRejected(key, rejectedEnv);
	}

	private Optional<BookingDetailsEntity> waitForBookingSnapshot(UUID bookingId) {
		Optional<BookingDetailsEntity> found = bookingDetailsRepo.findById(bookingId);
		if (found.isPresent()) return found;

		for (int i = 0; i < SNAPSHOT_MAX_ATTEMPTS; i++) {
			sleepQuietly(SNAPSHOT_SLEEP_MS);
			found = bookingDetailsRepo.findById(bookingId);
			if (found.isPresent()) return found;
		}
		return Optional.empty();
	}

	private void sleepQuietly(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	private UUID safeCorrelationId(EventEnvelope<?> message) {
		try {
			if (message == null || message.meta == null || message.meta.correlationId == null) {
				return UUID.randomUUID();
			}
			return UUID.fromString(message.meta.correlationId.asText());
		} catch (Exception e) {
			return UUID.randomUUID();
		}
	}

	/**
	 * Must match "data" shape of booking.created.v1.
	 */
	public record BookingCreatedPayload(
			UUID bookingId,
			String flightId,
			String seatClass
	) {}
}