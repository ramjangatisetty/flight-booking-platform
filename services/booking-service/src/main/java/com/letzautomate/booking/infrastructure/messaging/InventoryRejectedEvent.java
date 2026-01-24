package com.letzautomate.booking.infrastructure.messaging;

import java.util.UUID;

public class InventoryRejectedEvent {
	public UUID bookingId;
	public String status;
	public String reason;
}