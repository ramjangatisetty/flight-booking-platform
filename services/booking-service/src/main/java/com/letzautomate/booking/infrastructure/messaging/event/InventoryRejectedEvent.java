package com.letzautomate.booking.infrastructure.messaging.event;

import java.util.UUID;

public class InventoryRejectedEvent {
	public UUID bookingId;
	public String status;
	public String reason;
}
