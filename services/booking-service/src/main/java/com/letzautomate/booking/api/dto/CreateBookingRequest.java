package com.letzautomate.booking.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateBookingRequest {

	@NotBlank
	private String flightId;

	@NotNull
	private SeatClass seatClass;

	@NotNull @DecimalMin(value = "0.01")
	private BigDecimal amount;

	@NotBlank @Pattern(regexp = "^[A-Z]{3}$")
	private String currency; // Phase 1: "USD"

	public String getFlightId() { return flightId; }
	public void setFlightId(String flightId) { this.flightId = flightId; }

	public SeatClass getSeatClass() { return seatClass; }
	public void setSeatClass(SeatClass seatClass) { this.seatClass = seatClass; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
}

