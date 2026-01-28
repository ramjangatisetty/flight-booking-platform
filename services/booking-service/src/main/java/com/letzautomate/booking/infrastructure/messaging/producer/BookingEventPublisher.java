package com.letzautomate.booking.infrastructure.messaging.producer;

import com.letzautomate.booking.domain.model.Booking;
import com.letzautomate.booking.infrastructure.messaging.event.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class BookingEventPublisher {

	public static final String TOPIC_INVENTORY_RESERVE_REQUESTED_V1 = "inventory.reserve.requested.v1";
	public static final String TOPIC_INVENTORY_RELEASE_REQUESTED_V1 = "inventory.release.requested.v1";
	public static final String TOPIC_PAYMENT_REQUESTED_V1 = "payment.requested.v1";
	public static final String TOPIC_BOOKING_CONFIRMED_V1 = "booking.confirmed.v1";
	public static final String TOPIC_BOOKING_REJECTED_V1 = "booking.rejected.v1";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public BookingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishInventoryReserveRequested(Booking booking) {
		InventoryReserveRequestedEvent payload = new InventoryReserveRequestedEvent();
		payload.bookingId = booking.getBookingId();
		payload.flightId = booking.getFlightId();
		payload.seatClass = booking.getSeatClass();

		var envelope = EventEnvelope.of(
				"inventory.reserve.requested",
				1,
				booking.getCorrelationId(),
				"booking-service",
				payload
		);

		kafkaTemplate.send(TOPIC_INVENTORY_RESERVE_REQUESTED_V1, booking.getBookingId().toString(), envelope);
	}

	public void publishPaymentRequested(UUID bookingId, UUID correlationId, UUID reservationId, 
	                                     String flightId, String seatClass, BigDecimal amount, String currency) {
		PaymentRequestedEvent payload = new PaymentRequestedEvent();
		payload.bookingId = bookingId;
		payload.reservationId = reservationId;
		payload.flightId = flightId;
		payload.seatClass = seatClass;
		payload.amount = amount;
		payload.currency = currency;

		var envelope = EventEnvelope.of(
				"payment.requested",
				1,
				correlationId,
				"booking-service",
				payload
		);

		kafkaTemplate.send(TOPIC_PAYMENT_REQUESTED_V1, bookingId.toString(), envelope);
	}

	public void publishBookingConfirmed(UUID bookingId, UUID correlationId, UUID reservationId, UUID paymentId,
	                                     String flightId, String seatClass, BigDecimal amount, String currency) {
		BookingConfirmedEvent payload = new BookingConfirmedEvent();
		payload.bookingId = bookingId;
		payload.reservationId = reservationId;
		payload.paymentId = paymentId;
		payload.flightId = flightId;
		payload.seatClass = seatClass;
		payload.amount = amount;
		payload.currency = currency;
		payload.status = "CONFIRMED";
		payload.reason = null; // Always null for confirmed bookings
		
		// Add baggage integration fields
		payload.passengerId = bookingId; // Simplified: use bookingId as passengerId
		payload.origin = null; // Baggage service will derive from flightId
		payload.destination = null; // Baggage service will derive from flightId

		var envelope = EventEnvelope.of(
				"booking.confirmed",
				1,
				correlationId,
				"booking-service",
				payload
		);

		kafkaTemplate.send(TOPIC_BOOKING_CONFIRMED_V1, bookingId.toString(), envelope);
	}

	public void publishBookingRejected(UUID bookingId, UUID correlationId, UUID reservationId, UUID paymentId,
	                                    String flightId, String seatClass, BigDecimal amount, String currency, 
	                                    String reason) {
		BookingRejectedEvent payload = new BookingRejectedEvent();
		payload.bookingId = bookingId;
		payload.reservationId = reservationId; // null if rejected at inventory stage
		payload.paymentId = paymentId; // null if rejected before payment
		payload.flightId = flightId;
		payload.seatClass = seatClass;
		payload.amount = amount;
		payload.currency = currency;
		payload.status = "REJECTED";
		payload.reason = reason;

		var envelope = EventEnvelope.of(
				"booking.rejected",
				1,
				correlationId,
				"booking-service",
				payload
		);

		kafkaTemplate.send(TOPIC_BOOKING_REJECTED_V1, bookingId.toString(), envelope);
	}

	public void publishInventoryReleaseRequested(UUID bookingId, UUID correlationId, UUID reservationId, String reason) {
		InventoryReleaseRequestedEvent payload = new InventoryReleaseRequestedEvent();
		payload.bookingId = bookingId;
		payload.reservationId = reservationId;
		payload.reason = reason;

		var envelope = EventEnvelope.of(
				"inventory.release.requested",
				1,
				correlationId,
				"booking-service",
				payload
		);

		kafkaTemplate.send(TOPIC_INVENTORY_RELEASE_REQUESTED_V1, bookingId.toString(), envelope);
	}
}
