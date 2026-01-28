package com.letzautomate.booking.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Terminal event published when a booking is confirmed.
 * Has the same payload structure as BookingRejectedEvent for consistency.
 */
public class BookingConfirmedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public UUID paymentId;
	public String flightId;
	public String seatClass;
	public BigDecimal amount;
	public String currency;
	public String status; // CONFIRMED
	public String reason; // nullable - always null for confirmed bookings
	
	// Additional fields for baggage integration
	public UUID passengerId; // For now, use bookingId as passengerId (simplified)
	public String origin; // Derived from flightId or set to default
	public String destination; // Derived from flightId or set to default
}
