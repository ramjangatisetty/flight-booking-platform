package com.letzautomate.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "inventory_reservations",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_reservation_booking",
				columnNames = {"booking_id"}
		)
)
public class InventoryReservationEntity {

	@Id
	@GeneratedValue
	private UUID reservationId;

	@Column(name = "booking_id", nullable = false)
	private UUID bookingId;

	@Column(name = "flight_id", nullable = false)
	private String flightId;

	@Column(name = "seat_class", nullable = false)
	private String seatClass;

	@Column(name = "status", nullable = false)
	private String status;

	// ✅ NEW: store reject reason (NO_SEATS / INSUFFICIENT_FUNDS / MISSING_BOOKING_SNAPSHOT etc.)
	@Column(name = "reason")
	private String reason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	public UUID getReservationId() { return reservationId; }

	public UUID getBookingId() { return bookingId; }
	public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

	public String getFlightId() { return flightId; }
	public void setFlightId(String flightId) { this.flightId = flightId; }

	public String getSeatClass() { return seatClass; }
	public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }

	public Instant getCreatedAt() { return createdAt; }
}