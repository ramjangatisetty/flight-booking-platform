package com.letzautomate.booking.api.dto;

import java.util.UUID;

public class BookingStatusResponse {

	private UUID bookingId;
	private UUID correlationId;
	private String status;
	private String updatedAt;

	public BookingStatusResponse() {}

	public BookingStatusResponse(
			UUID bookingId,
			UUID correlationId,
			String status,
			String updatedAt
	) {
		this.bookingId = bookingId;
		this.correlationId = correlationId;
		this.status = status;
		this.updatedAt = updatedAt;
	}

	public UUID getBookingId() { return bookingId; }
	public UUID getCorrelationId() { return correlationId; }
	public String getStatus() { return status; }
	public String getUpdatedAt() { return updatedAt; }
}