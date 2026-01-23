package com.letzautomate.inventory.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentSucceededEvent {
	public UUID bookingId;
	public UUID paymentId;
	public String flightId;
	public String seatClass;
	public BigDecimal amount;
	public String currency;
	public String provider;
	public String status;
}