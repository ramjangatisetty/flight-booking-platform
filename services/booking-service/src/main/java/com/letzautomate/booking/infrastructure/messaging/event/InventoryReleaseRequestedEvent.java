package com.letzautomate.booking.infrastructure.messaging.event;

import java.util.UUID;

public class InventoryReleaseRequestedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public String reason;
}
