package com.letzautomate.booking.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentSucceededEvent {
	public UUID bookingId;
	public UUID paymentId;
	public BigDecimal amount;
	public String currency;
	public String provider;
	public String status; // SUCCEEDED
}
