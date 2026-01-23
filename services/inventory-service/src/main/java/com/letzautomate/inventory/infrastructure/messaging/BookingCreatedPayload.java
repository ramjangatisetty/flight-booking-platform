package com.letzautomate.inventory.infrastructure.messaging;

import java.util.UUID;

public record BookingCreatedPayload(
		UUID bookingId,
		String flightId,
		String seatClass
) {}
