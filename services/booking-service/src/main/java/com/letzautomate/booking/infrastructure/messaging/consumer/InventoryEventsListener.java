package com.letzautomate.booking.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.booking.application.BookingAppService;
import com.letzautomate.booking.infrastructure.messaging.event.*;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventsListener {

	public static final String TOPIC_INVENTORY_RESERVED_V1 = "inventory.reserved.v1";
	public static final String TOPIC_INVENTORY_REJECTED_V1 = "inventory.rejected.v1";
	public static final String TOPIC_PAYMENT_SUCCEEDED_V1 = "payment.succeeded.v1";
	public static final String TOPIC_PAYMENT_FAILED_V1 = "payment.failed.v1";

	private final BookingAppService bookingAppService;
	private final ObjectMapper objectMapper;

	public InventoryEventsListener(BookingAppService bookingAppService, ObjectMapper objectMapper) {
		this.bookingAppService = bookingAppService;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(
			topics = { TOPIC_INVENTORY_RESERVED_V1, TOPIC_INVENTORY_REJECTED_V1 },
			containerFactory = "inventoryReservedListenerFactory"
	)
	public void onInventoryEvent(EventEnvelope<?> message) {
		if (message == null || message.meta == null || message.data == null) return;

		String eventType = message.meta.eventType;
		if (eventType == null || eventType.isBlank()) return;

		UUID correlationId = message.meta.correlationId;

		if ("inventory.reserved".equalsIgnoreCase(eventType)) {

			InventoryReservedEvent evt =
					objectMapper.convertValue(message.data, InventoryReservedEvent.class);

			if (evt == null || evt.bookingId == null) return;

			bookingAppService.markInventoryReserved(
					evt.bookingId,
					correlationId,
					evt.reservationId
			);
			return;
		}

		if ("inventory.rejected".equalsIgnoreCase(eventType)) {

			InventoryRejectedEvent evt =
					objectMapper.convertValue(message.data, InventoryRejectedEvent.class);

			if (evt == null || evt.bookingId == null) return;

			bookingAppService.markInventoryRejected(
					evt.bookingId,
					correlationId,
					evt.reason
			);
		}
	}

	@KafkaListener(
			topics = { TOPIC_PAYMENT_SUCCEEDED_V1, TOPIC_PAYMENT_FAILED_V1 },
			containerFactory = "paymentListenerFactory"
	)
	public void onPaymentEvent(EventEnvelope<?> message) {
		if (message == null || message.meta == null || message.data == null) return;

		String eventType = message.meta.eventType;
		if (eventType == null || eventType.isBlank()) return;

		UUID correlationId = message.meta.correlationId;

		if ("payment.succeeded".equalsIgnoreCase(eventType)) {

			PaymentSucceededEvent evt =
					objectMapper.convertValue(message.data, PaymentSucceededEvent.class);

			if (evt == null || evt.bookingId == null) return;

			// First, confirm the booking (this commits the transaction)
			bookingAppService.markPaymentSucceeded(
					evt.bookingId,
					correlationId,
					evt.paymentId
			);
			
			// Then trigger loyalty accrual AFTER the booking confirmation transaction has committed.
			// This ensures bag_tag updates from baggage events won't be overwritten.
			bookingAppService.triggerLoyaltyAccrual(evt.bookingId);
			return;
		}

		if ("payment.failed".equalsIgnoreCase(eventType)) {

			PaymentFailedEvent evt =
					objectMapper.convertValue(message.data, PaymentFailedEvent.class);

			if (evt == null || evt.bookingId == null) return;

			bookingAppService.markPaymentFailed(
					evt.bookingId,
					correlationId,
					evt.reasonCode
			);
		}
	}
}
