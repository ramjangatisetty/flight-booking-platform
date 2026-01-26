package com.letzautomate.inventory.api.dto;

import java.time.Instant;
import java.util.UUID;

public class InventoryReservationResponse {
	private UUID bookingId;
	private UUID reservationId;
	private String status;
	private String reason;
	private String flightId;
	private String seatClass;
	private Instant createdAt;

	public InventoryReservationResponse() {}

	public InventoryReservationResponse(
			UUID bookingId,
			UUID reservationId,
			String status,
			String reason,
			String flightId,
			String seatClass,
			Instant createdAt
	) {
		this.bookingId = bookingId;
		this.reservationId = reservationId;
		this.status = status;
		this.reason = reason;
		this.flightId = flightId;
		this.seatClass = seatClass;
		this.createdAt = createdAt;
	}

	public UUID getBookingId() { return bookingId; }
	public UUID getReservationId() { return reservationId; }
	public String getStatus() { return status; }
	public String getReason() { return reason; }
	public String getFlightId() { return flightId; }
	public String getSeatClass() { return seatClass; }
	public Instant getCreatedAt() { return createdAt; }
}