package com.letzautomate.inventory.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.inventory.application.InventoryService;
import com.letzautomate.inventory.infrastructure.messaging.event.EventEnvelope;
import com.letzautomate.inventory.infrastructure.messaging.event.InventoryRejectedEvent;
import com.letzautomate.inventory.infrastructure.messaging.event.InventoryReleasedEvent;
import com.letzautomate.inventory.infrastructure.messaging.event.InventoryReservedEvent;
import com.letzautomate.inventory.infrastructure.messaging.producer.InventoryEventPublisher;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryProcessor {

	public static final String TOPIC_INVENTORY_RESERVE_REQUESTED_V1 = "inventory.reserve.requested.v1";
	public static final String TOPIC_INVENTORY_RELEASE_REQUESTED_V1 = "inventory.release.requested.v1";

	private final InventoryService inventoryService;
	private final InventoryEventPublisher publisher;
	private final ObjectMapper objectMapper;

	public InventoryProcessor(
			InventoryService inventoryService,
			InventoryEventPublisher publisher,
			ObjectMapper objectMapper
	) {
		this.inventoryService = inventoryService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	/**
	 * Consume inventory.reserve.requested.v1 and attempt to reserve inventory.
	 * Publish either inventory.reserved.v1 or inventory.rejected.v1
	 */
	@KafkaListener(
			topics = TOPIC_INVENTORY_RESERVE_REQUESTED_V1,
			containerFactory = "paymentSucceededListenerFactory"
	)
	public void onInventoryReserveRequested(EventEnvelope<?> message) {
		if (message == null || message.data == null || message.meta == null) return;

		InventoryReserveRequestedPayload payload = objectMapper.convertValue(message.data, InventoryReserveRequestedPayload.class);
		if (payload == null || payload.bookingId() == null) return;

		String key = payload.bookingId().toString();
		UUID correlationId = safeCorrelationId(message);

		var result = inventoryService.reserve(
				payload.bookingId(),
				payload.flightId(),
				payload.seatClass()
		);

		if ("RESERVED".equalsIgnoreCase(result.status())) {
			var reservedEnv = EventEnvelope.of(
					"inventory.reserved",
					1,
					correlationId,
					"inventory-service",
					InventoryReservedEvent.of(payload.bookingId(), result.reservationId())
			);
			publisher.publishReserved(key, reservedEnv);
		} else {
			InventoryRejectedEvent rej = new InventoryRejectedEvent();
			rej.bookingId = payload.bookingId();
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
	 * Consume inventory.release.requested.v1 for compensation.
	 * Release the reservation and publish inventory.released.v1
	 */
	@KafkaListener(
			topics = TOPIC_INVENTORY_RELEASE_REQUESTED_V1,
			containerFactory = "paymentSucceededListenerFactory"
	)
	public void onInventoryReleaseRequested(EventEnvelope<?> message) {
		if (message == null || message.data == null || message.meta == null) return;

		InventoryReleaseRequestedPayload payload = objectMapper.convertValue(message.data, InventoryReleaseRequestedPayload.class);
		if (payload == null || payload.bookingId() == null) return;

		String key = payload.bookingId().toString();
		UUID correlationId = safeCorrelationId(message);

		System.out.println("✓ Received inventory.release.requested.v1 for booking: " + payload.bookingId() + 
		                   ", reservationId: " + payload.reservationId() + ", reason: " + payload.reason());

		var result = inventoryService.release(
				payload.bookingId(),
				payload.reservationId()
		);

		// Always publish inventory.released.v1 (even if already released - idempotency)
		InventoryReleasedEvent releasedEvent = new InventoryReleasedEvent();
		releasedEvent.bookingId = payload.bookingId();
		releasedEvent.reservationId = result.reservationId();
		releasedEvent.status = "RELEASED";

		var releasedEnv = EventEnvelope.of(
				"inventory.released",
				1,
				correlationId,
				"inventory-service",
				releasedEvent
		);
		
		publisher.publishReleased(key, releasedEnv);
		
		System.out.println("✓ Published inventory.released.v1 for booking: " + payload.bookingId() + 
		                   ", reservationId: " + result.reservationId());
	}

	private UUID safeCorrelationId(EventEnvelope<?> message) {
		try {
			if (message == null || message.meta == null || message.meta.correlationId == null) {
				return UUID.randomUUID();
			}
			return message.meta.correlationId;
		} catch (Exception e) {
			return UUID.randomUUID();
		}
	}

	/**
	 * Payload for inventory.reserve.requested.v1
	 */
	public record InventoryReserveRequestedPayload(
			UUID bookingId,
			String flightId,
			String seatClass
	) {}

	/**
	 * Payload for inventory.release.requested.v1
	 */
	public record InventoryReleaseRequestedPayload(
			UUID bookingId,
			UUID reservationId,
			String reason
	) {}
}