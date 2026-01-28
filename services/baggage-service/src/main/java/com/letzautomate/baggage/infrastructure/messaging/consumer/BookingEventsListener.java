package com.letzautomate.baggage.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.baggage.application.BaggageService;
import com.letzautomate.baggage.infrastructure.messaging.event.BookingConfirmedEvent;
import com.letzautomate.baggage.infrastructure.messaging.event.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventsListener {

	private static final Logger log = LoggerFactory.getLogger(BookingEventsListener.class);
	private final BaggageService baggageService;
	private final ObjectMapper objectMapper;

	public BookingEventsListener(BaggageService baggageService, ObjectMapper objectMapper) {
		this.baggageService = baggageService;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "booking.confirmed.v1", groupId = "baggage-service-v2")
	public void handleBookingEvent(EventEnvelope<?> envelope) {
		if (envelope == null || envelope.meta == null || envelope.data == null) {
			log.warn("Received null or incomplete event envelope");
			return;
		}

		String eventType = envelope.meta.eventType;
		log.info("Received event: eventType={}, eventId={}, correlationId={}", 
				eventType, envelope.meta.eventId, envelope.meta.correlationId);

		if ("booking.confirmed".equals(eventType)) {
			handleBookingConfirmed(envelope);
		} else {
			log.debug("Ignoring event type: {}", eventType);
		}
	}

	private void handleBookingConfirmed(EventEnvelope<?> envelope) {
		BookingConfirmedEvent event = objectMapper.convertValue(envelope.data, BookingConfirmedEvent.class);
		
		if (event == null || event.bookingId == null) {
			log.warn("Invalid BookingConfirmedEvent: missing required fields");
			return;
		}
		
		try {
			log.info("Processing booking.confirmed: bookingId={}, flightId={}", 
					event.bookingId, event.flightId);
			
			// Auto-create baggage for confirmed booking
			String bagTag = baggageService.createBaggageForBooking(
					event.bookingId,
					event.passengerId,
					event.flightId,
					envelope.meta.correlationId
			);
			
			log.info("Baggage auto-created: bookingId={}, bagTag={}", event.bookingId, bagTag);
			
		} catch (Exception e) {
			log.error("Failed to create baggage for booking: bookingId={}", event.bookingId, e);
			// In production, you might want to publish a compensation event or retry
		}
	}
}
