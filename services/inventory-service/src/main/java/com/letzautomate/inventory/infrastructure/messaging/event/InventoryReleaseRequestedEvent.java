package com.letzautomate.inventory.infrastructure.messaging.event;

import java.util.UUID;

public class InventoryReleaseRequestedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public String reason;
}
