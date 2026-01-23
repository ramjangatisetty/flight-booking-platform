package com.letzautomate.inventory.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.inventory.infrastructure.messaging.EventEnvelope;
import com.letzautomate.inventory.infrastructure.messaging.InventoryEventPublisher;
import com.letzautomate.inventory.infrastructure.messaging.InventoryRejectedEvent;
import com.letzautomate.inventory.infrastructure.messaging.InventoryReservedEvent;
import com.letzautomate.inventory.infrastructure.messaging.PaymentSucceededEvent;
import com.letzautomate.inventory.infrastructure.persistence.BookingDetailsEntity;
import com.letzautomate.inventory.infrastructure.persistence.BookingDetailsRepository;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryProcessor {

	public static final String TOPIC_BOOKING_CREATED_V1 = "booking.created.v1";
	public static final String TOPIC_PAYMENT_SUCCEEDED_V1 = "payment.succeeded.v1";

	private final InventoryService inventoryService;
	private final InventoryEventPublisher publisher;
	private final BookingDetailsRepository bookingDetailsRepo;
	private final ObjectMapper objectMapper;

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
	 * Option B - Step 1:
	 * Consume booking.created and store bookingId -> flightId, seatClass in H2.
	 *
	 * NOTE:
	 * - We reuse "paymentSucceededListenerFactory" because your KafkaConfig defines only that factory.
	 * - message.meta might be present or absent depending on producer; we don't require meta here.
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
					// Upsert (safe if the event is replayed)
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
	 * Option B - Step 2:
	 * Consume payment.succeeded, lookup booking details from H2, reserve, publish inventory event.
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

		var bookingDetailsOpt = bookingDetailsRepo.findById(payment.bookingId);
		if (bookingDetailsOpt.isEmpty()) {
			// Payment arrived but booking snapshot not found -> reject
			var rejectedEnv = EventEnvelope.of(
					"inventory.rejected",
					1,
					correlationId,
					"inventory-service",
					InventoryRejectedEvent.of(payment.bookingId)
			);
			publisher.publishRejected(key, rejectedEnv);
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
			var rejectedEnv = EventEnvelope.of(
					"inventory.rejected",
					1,
					correlationId,
					"inventory-service",
					InventoryRejectedEvent.of(payment.bookingId)
			);
			publisher.publishRejected(key, rejectedEnv);
		}
	}

	/*private UUID safeCorrelationId(EventEnvelope<?> message) {
		if (message == null || message.meta == null || message.meta.correlationId == null) {
			return UUID.randomUUID();
		}
		return message.meta.correlationId;
	}*/

	private UUID safeCorrelationId(EventEnvelope<?> message) {
		try {
			if (message == null || message.meta == null || message.meta.correlationId == null) {
				return UUID.randomUUID();
			}

			// correlationId may come as UUID or String → JsonNode
			return UUID.fromString(message.meta.correlationId.asText());
		} catch (Exception e) {
			return UUID.randomUUID();
		}
	}

	/**
	 * Keep this record here (simplest) OR move it to infrastructure.messaging package if you prefer.
	 * This must match the "data" shape of booking.created.v1.
	 */
	public record BookingCreatedPayload(
			UUID bookingId,
			String flightId,
			String seatClass
	) {}
}