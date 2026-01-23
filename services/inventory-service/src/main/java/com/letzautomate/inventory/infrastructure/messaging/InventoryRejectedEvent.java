package com.letzautomate.inventory.infrastructure.messaging;

import java.util.UUID;

public class InventoryRejectedEvent {
	public UUID bookingId;
	public String status; // REJECTED
	public String reason; // optional

	public static InventoryRejectedEvent of(UUID bookingId) {
		InventoryRejectedEvent e = new InventoryRejectedEvent();
		e.bookingId = bookingId;
		e.status = "REJECTED";
		e.reason = "NO_SEATS";
		return e;
	}
}