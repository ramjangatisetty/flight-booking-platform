package com.letzautomate.booking.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letzautomate.booking.application.BookingAppService;
import com.letzautomate.booking.infrastructure.messaging.event.BaggageCheckedInEvent;
import com.letzautomate.booking.infrastructure.messaging.event.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BaggageEventsListener {

	private static final Logger log = LoggerFactory.getLogger(BaggageEventsListener.class);
	private final BookingAppService bookingAppService;
	private final ObjectMapper objectMapper;

	public BaggageEventsListener(BookingAppService bookingAppService, ObjectMapper objectMapper) {
		this.bookingAppService = bookingAppService;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "baggage.events", groupId = "booking-service-baggage", containerFactory = "baggageListenerFactory")
	public void handleBaggageEvent(EventEnvelope<?> envelope) {
		log.info(">>> BaggageEventsListener.handleBaggageEvent ENTERED");
		if (envelope == null || envelope.meta == null || envelope.data == null) {
			log.warn("Received null or incomplete event envelope");
			return;
		}

		String eventType = envelope.meta.eventType;
		log.info("Received baggage event: eventType={}, eventId={}, correlationId={}", 
				eventType, envelope.meta.eventId, envelope.meta.correlationId);

		if ("baggage.checked_in.v1".equals(eventType)) {
			handleBaggageCheckedIn(envelope);
		} else {
			log.debug("Ignoring event type: {}", eventType);
		}
		log.info(">>> BaggageEventsListener.handleBaggageEvent COMPLETED");
	}

	private void handleBaggageCheckedIn(EventEnvelope<?> envelope) {
		BaggageCheckedInEvent event = objectMapper.convertValue(envelope.data, BaggageCheckedInEvent.class);
		
		if (event == null || event.bookingId == null) {
			log.warn("Invalid BaggageCheckedInEvent: missing required fields");
			return;
		}
		
		try {
			log.info("Processing baggage.checked_in.v1: bookingId={}, bagTag={}", 
					event.bookingId, event.bagTag);
			
			bookingAppService.updateBagTag(event.bookingId, event.bagTag);
			
			log.info("Booking updated with bagTag: bookingId={}, bagTag={}", event.bookingId, event.bagTag);
			
		} catch (Exception e) {
			log.error("Failed to update booking with bagTag: bookingId={}", event.bookingId, e);
			throw e; // Re-throw to trigger transaction rollback
		}
	}
}
