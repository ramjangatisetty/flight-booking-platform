package com.letzautomate.booking.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentFailedEvent {
	public UUID bookingId;
	public UUID paymentId;
	public BigDecimal amount;
	public String currency;
	public String provider;
	public String status; // FAILED
	public String reasonCode;
	public String reasonMessage;
}
