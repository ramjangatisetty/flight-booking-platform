package com.letzautomate.payment.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public class BookingCreatedEvent {
	public UUID bookingId;
	public String flightId;
	public String seatClass;
	public BigDecimal amount;
	public String currency;
}
