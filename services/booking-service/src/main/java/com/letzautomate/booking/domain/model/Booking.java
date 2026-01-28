package com.letzautomate.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Booking {
	private final UUID bookingId;
	private final UUID correlationId;
	private final String flightId;
	private final String seatClass;
	private final BigDecimal amount;
	private final String currency;
	private BookingStatus status;
	private final Instant createdAt;
	private Instant updatedAt;

	public Booking(UUID bookingId, UUID correlationId, String flightId, String seatClass,
				   BigDecimal amount, String currency, BookingStatus status,
				   Instant createdAt, Instant updatedAt) {
		this.bookingId = bookingId;
		this.correlationId = correlationId;
		this.flightId = flightId;
		this.seatClass = seatClass;
		this.amount = amount;
		this.currency = currency;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getBookingId() { return bookingId; }
	public UUID getCorrelationId() { return correlationId; }
	public String getFlightId() { return flightId; }
	public String getSeatClass() { return seatClass; }
	public BigDecimal getAmount() { return amount; }
	public String getCurrency() { return currency; }
	public BookingStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void markConfirmed() {
		this.status = BookingStatus.CONFIRMED;
		this.updatedAt = Instant.now();
	}

	public void markFailed() {
		this.status = BookingStatus.FAILED;
		this.updatedAt = Instant.now();
	}
}
