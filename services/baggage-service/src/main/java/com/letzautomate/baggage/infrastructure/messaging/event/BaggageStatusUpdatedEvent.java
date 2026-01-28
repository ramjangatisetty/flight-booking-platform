package com.letzautomate.baggage.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when baggage status is updated.
 */
public class BaggageStatusUpdatedEvent {
	public UUID bookingId;
	public String bagTag;
	public String previousStatus;
	public String newStatus;
	public String airport;
	public Instant updatedAt;
}
