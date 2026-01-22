package com.letzautomate.booking.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BookingResponse {
	private UUID bookingId;
	private UUID correlationId;
	private String flightId;
	private SeatClass seatClass;
	private BigDecimal amount;
	private String currency;
	private String status;
	private Instant createdAt;
	private Instant updatedAt;

	public UUID getBookingId() { return bookingId; }
	public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

	public UUID getCorrelationId() { return correlationId; }
	public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

	public String getFlightId() { return flightId; }
	public void setFlightId(String flightId) { this.flightId = flightId; }

	public SeatClass getSeatClass() { return seatClass; }
	public void setSeatClass(SeatClass seatClass) { this.seatClass = seatClass; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
