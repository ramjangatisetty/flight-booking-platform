package com.letzautomate.inventory.infrastructure.messaging;

import java.util.UUID;

public class InventoryRejectedEvent {
	public UUID bookingId;
	public String status; // REJECTED
	public String reason; // NO_SEATS / PAYMENT_FAILED / INSUFFICIENT_FUNDS / etc.

	public static InventoryRejectedEvent of(UUID bookingId) {
		return of(bookingId, "NO_SEATS");
	}

	public static InventoryRejectedEvent of(UUID bookingId, String reason) {
		InventoryRejectedEvent e = new InventoryRejectedEvent();
		e.bookingId = bookingId;
		e.status = "REJECTED";
		e.reason = reason;
		return e;
	}
}