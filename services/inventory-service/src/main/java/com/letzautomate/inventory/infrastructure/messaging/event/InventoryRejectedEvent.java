package com.letzautomate.inventory.infrastructure.messaging.event;

import java.util.UUID;

public class InventoryRejectedEvent {
	public UUID bookingId;
	public String status;  // REJECTED
	public String reason;

	public static InventoryRejectedEvent of(UUID bookingId, String reason) {
		InventoryRejectedEvent e = new InventoryRejectedEvent();
		e.bookingId = bookingId;
		e.status = "REJECTED";
		e.reason = reason;
		return e;
	}
}