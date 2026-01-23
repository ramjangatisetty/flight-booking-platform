package com.letzautomate.inventory.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "booking_details")
public class BookingDetailsEntity {

	@Id
	@Column(name = "booking_id", nullable = false)
	private UUID bookingId;

	@Column(name = "flight_id", nullable = false)
	private String flightId;

	@Column(name = "seat_class", nullable = false)
	private String seatClass;

	public UUID getBookingId() {
		return bookingId;
	}

	public void setBookingId(UUID bookingId) {
		this.bookingId = bookingId;
	}

	public String getFlightId() {
		return flightId;
	}

	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}

	public String getSeatClass() {
		return seatClass;
	}

	public void setSeatClass(String seatClass) {
		this.seatClass = seatClass;
	}
}
