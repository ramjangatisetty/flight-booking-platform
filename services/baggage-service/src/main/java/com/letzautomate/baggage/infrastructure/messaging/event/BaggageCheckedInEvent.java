package com.letzautomate.baggage.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when baggage is checked in.
 * Published back to Kafka for other services to consume.
 */
public class BaggageCheckedInEvent {
	public UUID bookingId;
	public UUID passengerId;
	public String bagTag;
	public String origin;
	public String destination;
	public String status; // CHECKED_IN
	public Instant checkedInAt;
}
