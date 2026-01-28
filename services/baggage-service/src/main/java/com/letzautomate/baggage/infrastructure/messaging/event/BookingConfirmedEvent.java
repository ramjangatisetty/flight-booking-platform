package com.letzautomate.baggage.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event received when a booking is confirmed.
 * Baggage service listens to this event to auto-create baggage records.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingConfirmedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public UUID paymentId;
	public String flightId;
	public String seatClass;
	public BigDecimal amount;
	public String currency;
	public String status;
	public String reason;
	
	// Additional fields for baggage creation
	public UUID passengerId;
	public String origin;
	public String destination;
}
