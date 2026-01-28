package com.letzautomate.baggage.application;

import com.letzautomate.baggage.api.dto.BaggageCheckinRequest;
import com.letzautomate.baggage.api.dto.BaggageCheckinResponse;
import com.letzautomate.baggage.api.dto.BaggageEvent;
import com.letzautomate.baggage.api.dto.BaggageStatusUpdateRequest;
import com.letzautomate.baggage.api.dto.BaggageStatusUpdateResponse;
import com.letzautomate.baggage.api.dto.BaggageTrackResponse;
import com.letzautomate.baggage.infrastructure.messaging.event.BaggageCheckedInEvent;
import com.letzautomate.baggage.infrastructure.messaging.event.BaggageStatusUpdatedEvent;
import com.letzautomate.baggage.infrastructure.messaging.producer.BaggageEventPublisher;
import com.letzautomate.baggage.infrastructure.persistence.entity.BagEntity;
import com.letzautomate.baggage.infrastructure.persistence.entity.BagEventEntity;
import com.letzautomate.baggage.infrastructure.persistence.repository.BagEventRepository;
import com.letzautomate.baggage.infrastructure.persistence.repository.BagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BaggageService {

	private static final Logger log = LoggerFactory.getLogger(BaggageService.class);
	private static final Random RANDOM = new Random();

	private final BagRepository bagRepository;
	private final BagEventRepository bagEventRepository;
	private final BaggageEventPublisher eventPublisher;

	public BaggageService(BagRepository bagRepository, 
	                      BagEventRepository bagEventRepository,
	                      BaggageEventPublisher eventPublisher) {
		this.bagRepository = bagRepository;
		this.bagEventRepository = bagEventRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public BaggageCheckinResponse checkinBaggage(BaggageCheckinRequest request) {
		String bagTag = request.getBagTag();
		
		// IDEMPOTENCY: Check if bag already exists
		Optional<BagEntity> existing = bagRepository.findById(bagTag);
		if (existing.isPresent()) {
			BagEntity bag = existing.get();
			log.info("Bag already checked in (idempotent). bagTag={}, status={}", bagTag, bag.getStatus());
			return new BaggageCheckinResponse(bag.getBagTag(), bag.getStatus(), bag.getCreatedAt());
		}

		// Create new bag
		BagEntity bag = new BagEntity();
		bag.setBagTag(bagTag);
		bag.setBookingId(UUID.fromString(request.getBookingId()));
		bag.setPassengerId(UUID.fromString(request.getPassengerId()));
		bag.setOrigin(request.getOrigin());
		bag.setDestination(request.getDestination());
		bag.setStatus("CHECKED_IN");
		bag.setCreatedAt(Instant.now());
		
		bagRepository.save(bag);

		// Create initial CHECKED_IN event
		BagEventEntity event = new BagEventEntity();
		event.setBagTag(bagTag);
		event.setEventType("CHECKED_IN");
		event.setAirport(request.getOrigin());
		event.setOccurredAt(bag.getCreatedAt());
		
		bagEventRepository.save(event);

		log.info("Bag checked in successfully. bagTag={}, origin={}, destination={}", 
				bagTag, request.getOrigin(), request.getDestination());

		return new BaggageCheckinResponse(bag.getBagTag(), bag.getStatus(), bag.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public BaggageTrackResponse trackBaggage(String bagTag) {
		BagEntity bag = bagRepository.findById(bagTag)
				.orElseThrow(() -> new BagNotFoundException("Bag not found: " + bagTag));

		List<BagEventEntity> eventEntities = bagEventRepository.findByBagTagOrderByOccurredAtAsc(bagTag);
		
		List<BaggageEvent> events = eventEntities.stream()
				.map(e -> new BaggageEvent(e.getEventType(), e.getAirport(), e.getOccurredAt()))
				.collect(Collectors.toList());

		return new BaggageTrackResponse(bag.getBagTag(), bag.getStatus(), events);
	}

	@Transactional
	public BaggageStatusUpdateResponse updateBaggageStatus(String bagTag, BaggageStatusUpdateRequest request) {
		BagEntity bag = bagRepository.findById(bagTag)
				.orElseThrow(() -> new BagNotFoundException("Bag not found: " + bagTag));

		String previousStatus = bag.getStatus();
		String newStatus = request.getStatus();
		Instant now = Instant.now();

		// Update bag status
		bag.setStatus(newStatus);
		bagRepository.save(bag);

		// Create status change event
		BagEventEntity event = new BagEventEntity();
		event.setBagTag(bagTag);
		event.setEventType(newStatus);
		event.setAirport(request.getAirport());
		event.setOccurredAt(now);
		bagEventRepository.save(event);

		log.info("Bag status updated. bagTag={}, previousStatus={}, newStatus={}, airport={}", 
				bagTag, previousStatus, newStatus, request.getAirport());

		// Publish status updated event
		BaggageStatusUpdatedEvent statusEvent = new BaggageStatusUpdatedEvent();
		statusEvent.bookingId = bag.getBookingId();
		statusEvent.bagTag = bagTag;
		statusEvent.previousStatus = previousStatus;
		statusEvent.newStatus = newStatus;
		statusEvent.airport = request.getAirport();
		statusEvent.updatedAt = now;
		
		eventPublisher.publishBaggageStatusUpdated(statusEvent, UUID.randomUUID());

		return new BaggageStatusUpdateResponse(bag.getBagTag(), bag.getStatus(), now);
	}

	/**
	 * Auto-create baggage for a confirmed booking.
	 * Called by Kafka event listener when booking.confirmed.v1 is received.
	 */
	@Transactional
	public String createBaggageForBooking(UUID bookingId, UUID passengerId, String flightId, UUID correlationId) {
		// Generate unique bagTag
		String bagTag = generateBagTag(flightId);
		
		// Check if baggage already exists for this booking (idempotency)
		Optional<BagEntity> existing = bagRepository.findByBookingId(bookingId);
		if (existing.isPresent()) {
			log.info("Baggage already exists for booking (idempotent). bookingId={}, bagTag={}", 
					bookingId, existing.get().getBagTag());
			return existing.get().getBagTag();
		}

		// Extract origin and destination from flightId (format: AA123)
		// In a real system, you'd query flight service for route details
		// For demo, we'll use default airports
		String origin = extractOriginFromFlightId(flightId);
		String destination = extractDestinationFromFlightId(flightId);

		Instant now = Instant.now();

		// Create bag entity
		BagEntity bag = new BagEntity();
		bag.setBagTag(bagTag);
		bag.setBookingId(bookingId);
		bag.setPassengerId(passengerId);
		bag.setOrigin(origin);
		bag.setDestination(destination);
		bag.setStatus("CHECKED_IN");
		bag.setCreatedAt(now);
		
		bagRepository.save(bag);

		// Create initial CHECKED_IN event
		BagEventEntity event = new BagEventEntity();
		event.setBagTag(bagTag);
		event.setEventType("CHECKED_IN");
		event.setAirport(origin);
		event.setOccurredAt(now);
		
		bagEventRepository.save(event);

		log.info("Baggage auto-created for booking. bookingId={}, bagTag={}, origin={}, destination={}", 
				bookingId, bagTag, origin, destination);

		// Publish baggage checked-in event
		BaggageCheckedInEvent checkedInEvent = new BaggageCheckedInEvent();
		checkedInEvent.bookingId = bookingId;
		checkedInEvent.passengerId = passengerId;
		checkedInEvent.bagTag = bagTag;
		checkedInEvent.origin = origin;
		checkedInEvent.destination = destination;
		checkedInEvent.status = "CHECKED_IN";
		checkedInEvent.checkedInAt = now;
		
		eventPublisher.publishBaggageCheckedIn(checkedInEvent, correlationId);

		return bagTag;
	}

	/**
	 * Generate a unique bag tag based on flight ID.
	 * Format: [Airline Code][8 random digits]
	 * Example: AA12345678
	 */
	private String generateBagTag(String flightId) {
		// Extract airline code from flightId (e.g., "AA123" -> "AA")
		String airlineCode = flightId.length() >= 2 ? flightId.substring(0, 2).toUpperCase() : "XX";
		
		// Generate 8 random digits
		int randomNumber = 10000000 + RANDOM.nextInt(90000000);
		
		return airlineCode + randomNumber;
	}

	/**
	 * Extract origin airport from flight ID.
	 * In a real system, this would query the flight service.
	 * For demo purposes, we use a simple mapping.
	 */
	private String extractOriginFromFlightId(String flightId) {
		// Simple demo logic: map airline codes to common origins
		if (flightId.startsWith("AA")) return "DFW"; // American Airlines - Dallas
		if (flightId.startsWith("UA")) return "ORD"; // United - Chicago
		if (flightId.startsWith("DL")) return "ATL"; // Delta - Atlanta
		return "JFK"; // Default
	}

	/**
	 * Extract destination airport from flight ID.
	 * In a real system, this would query the flight service.
	 * For demo purposes, we use a simple mapping.
	 */
	private String extractDestinationFromFlightId(String flightId) {
		// Simple demo logic: common destinations
		if (flightId.startsWith("AA")) return "LAX";
		if (flightId.startsWith("UA")) return "SFO";
		if (flightId.startsWith("DL")) return "JFK";
		return "LAX"; // Default
	}

	@Transactional
	public String seedDemoData() {
		String bagTag = "AA12345678";
		
		// Check if already seeded
		if (bagRepository.findById(bagTag).isPresent()) {
			log.info("Demo data already seeded. bagTag={}", bagTag);
			return bagTag;
		}

		// Create bag
		BagEntity bag = new BagEntity();
		bag.setBagTag(bagTag);
		bag.setBookingId(UUID.randomUUID());
		bag.setPassengerId(UUID.randomUUID());
		bag.setOrigin("JFK");
		bag.setDestination("LAX");
		bag.setStatus("IN_TRANSIT");
		bag.setCreatedAt(Instant.now().minusSeconds(3600)); // 1 hour ago
		
		bagRepository.save(bag);

		// Create events
		Instant baseTime = bag.getCreatedAt();
		
		BagEventEntity event1 = new BagEventEntity();
		event1.setBagTag(bagTag);
		event1.setEventType("CHECKED_IN");
		event1.setAirport("JFK");
		event1.setOccurredAt(baseTime);
		bagEventRepository.save(event1);

		BagEventEntity event2 = new BagEventEntity();
		event2.setBagTag(bagTag);
		event2.setEventType("LOADED");
		event2.setAirport("JFK");
		event2.setOccurredAt(baseTime.plusSeconds(1800)); // 30 min later
		bagEventRepository.save(event2);

		BagEventEntity event3 = new BagEventEntity();
		event3.setBagTag(bagTag);
		event3.setEventType("IN_TRANSIT");
		event3.setAirport(null);
		event3.setOccurredAt(baseTime.plusSeconds(2400)); // 40 min later
		bagEventRepository.save(event3);

		log.info("Demo data seeded successfully. bagTag={}", bagTag);
		return bagTag;
	}

	public static class BagNotFoundException extends RuntimeException {
		public BagNotFoundException(String message) {
			super(message);
		}
	}
}
