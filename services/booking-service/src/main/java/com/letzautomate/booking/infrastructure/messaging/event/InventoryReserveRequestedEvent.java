package com.letzautomate.booking.infrastructure.messaging.event;

import java.util.UUID;

public class InventoryReserveRequestedEvent {
	public UUID bookingId;
	public String flightId;
	public String seatClass;
}
