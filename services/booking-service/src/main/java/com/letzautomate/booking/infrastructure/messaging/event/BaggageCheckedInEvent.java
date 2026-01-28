package com.letzautomate.booking.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * Event received when baggage is checked in.
 * Booking service listens to this to update booking with bagTag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaggageCheckedInEvent {
	public UUID bookingId;
	public UUID passengerId;
	public String bagTag;
	public String origin;
	public String destination;
	public String status;
	public Instant checkedInAt;
}
