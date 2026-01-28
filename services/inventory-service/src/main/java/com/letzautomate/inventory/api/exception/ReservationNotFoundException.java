package com.letzautomate.inventory.api.exception;

import java.util.UUID;

/**
 * Exception thrown when a reservation is not found for a given bookingId or reservationId.
 * Results in HTTP 404 with structured JSON error response.
 */
public class ReservationNotFoundException extends RuntimeException {

	private final UUID bookingId;
	private final UUID reservationId;
	private final LookupType lookupType;

	public enum LookupType {
		BY_BOOKING_ID,
		BY_RESERVATION_ID
	}

	/**
	 * Constructor for bookingId-based lookup
	 */
	public ReservationNotFoundException(UUID bookingId) {
		super("No inventory reservation exists for bookingId " + bookingId);
		this.bookingId = bookingId;
		this.reservationId = null;
		this.lookupType = LookupType.BY_BOOKING_ID;
	}

	/**
	 * Constructor for reservationId-based lookup
	 */
	public static ReservationNotFoundException forReservationId(UUID reservationId) {
		return new ReservationNotFoundException(reservationId, LookupType.BY_RESERVATION_ID);
	}

	private ReservationNotFoundException(UUID reservationId, LookupType lookupType) {
		super("No inventory reservation exists for reservationId " + reservationId);
		this.bookingId = null;
		this.reservationId = reservationId;
		this.lookupType = lookupType;
	}

	public UUID getBookingId() {
		return bookingId;
	}

	public UUID getReservationId() {
		return reservationId;
	}

	public LookupType getLookupType() {
		return lookupType;
	}
}
