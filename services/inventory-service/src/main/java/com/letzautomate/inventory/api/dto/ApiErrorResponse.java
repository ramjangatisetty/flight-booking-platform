package com.letzautomate.inventory.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

/**
 * Standardized error response for all inventory API errors.
 * Uses @JsonInclude to only include non-null fields in the response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
		String error,
		String message,
		UUID bookingId,
		UUID reservationId
) {
	/**
	 * Constructor for bookingId-based errors
	 */
	public ApiErrorResponse(String error, String message, UUID bookingId) {
		this(error, message, bookingId, null);
	}

	/**
	 * Constructor for reservationId-based errors (using named parameter pattern)
	 */
	public static ApiErrorResponse forReservationId(String error, String message, UUID reservationId) {
		return new ApiErrorResponse(error, message, null, reservationId);
	}
}
