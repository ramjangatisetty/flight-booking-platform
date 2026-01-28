package com.letzautomate.booking.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequestedEvent {
	public UUID bookingId;
	public UUID reservationId;
	public String flightId;
	public String seatClass;
	public BigDecimal amount;
	public String currency;
}
