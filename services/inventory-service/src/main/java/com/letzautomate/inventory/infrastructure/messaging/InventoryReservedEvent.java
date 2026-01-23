package com.letzautomate.inventory.infrastructure.messaging;

import java.util.UUID;

public class InventoryReservedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public String status; // RESERVED

	public static InventoryReservedEvent of(UUID bookingId, UUID reservationId) {
		InventoryReservedEvent e = new InventoryReservedEvent();
		e.bookingId = bookingId;
		e.reservationId = reservationId;
		e.status = "RESERVED";
		return e;
	}
}