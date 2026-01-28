package com.letzautomate.inventory.api.exception;

import com.letzautomate.inventory.api.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for consistent error responses across all controllers.
 * Centralizes error handling logic to ensure uniform API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ReservationNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleReservationNotFound(ReservationNotFoundException ex) {
		// Return appropriate response based on lookup type
		if (ex.getLookupType() == ReservationNotFoundException.LookupType.BY_BOOKING_ID) {
			return new ApiErrorResponse(
					"RESERVATION_NOT_FOUND",
					ex.getMessage(),
					ex.getBookingId()
			);
		} else {
			return ApiErrorResponse.forReservationId(
					"RESERVATION_NOT_FOUND",
					ex.getMessage(),
					ex.getReservationId()
			);
		}
	}
}
