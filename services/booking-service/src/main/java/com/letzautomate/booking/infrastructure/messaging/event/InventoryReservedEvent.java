package com.letzautomate.booking.infrastructure.messaging.event;

import java.util.UUID;

public class InventoryReservedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public String status;
}
