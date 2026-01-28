package com.letzautomate.booking.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Terminal event published when a booking is rejected.
 * Has the same payload structure as BookingConfirmedEvent for consistency.
 */
public class BookingRejectedEvent {
	public UUID bookingId;
	public UUID reservationId; // nullable - null if rejected at inventory stage
	public UUID paymentId; // nullable - null if rejected before payment
	public String flightId;
	public String seatClass;
	public BigDecimal amount;
	public String currency;
	public String status; // REJECTED
	public String reason; // Rejection reason (e.g., NO_SEATS, INSUFFICIENT_FUNDS)
}
