package com.letzautomate.booking.infrastructure.messaging;


import com.letzautomate.booking.domain.Booking;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

	public static final String TOPIC_BOOKING_CREATED_V1 = "booking.created.v1";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public BookingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishBookingCreated(Booking booking) {
		BookingCreatedEvent payload = new BookingCreatedEvent();
		payload.bookingId = booking.getBookingId();
		payload.flightId = booking.getFlightId();
		payload.seatClass = booking.getSeatClass();
		payload.amount = booking.getAmount();
		payload.currency = booking.getCurrency();

		var envelope = EventEnvelope.of("booking.created", 1, booking.getCorrelationId(), "booking-service", payload);

		// key = bookingId to keep ordering
		kafkaTemplate.send(TOPIC_BOOKING_CREATED_V1, booking.getBookingId().toString(), envelope);
	}
}
