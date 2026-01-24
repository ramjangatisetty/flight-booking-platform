package com.letzautomate.booking.api;

import com.letzautomate.booking.api.dto.BookingResponse;
import com.letzautomate.booking.api.dto.BookingStatusResponse;
import com.letzautomate.booking.api.dto.CreateBookingRequest;
import com.letzautomate.booking.application.BookingAppService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

	private final BookingAppService bookingAppService;

	public BookingController(BookingAppService bookingAppService) {
		this.bookingAppService = bookingAppService;
	}

	@PostMapping(consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public BookingResponse create(
			@Valid @RequestBody CreateBookingRequest request,
			@RequestHeader(value = "X-Correlation-Id", required = false) UUID correlationId
	) {
		return bookingAppService.createBooking(request, correlationId);
	}

	// Full booking details
	@GetMapping(value = "/{bookingId}", produces = "application/json")
	public BookingResponse get(@PathVariable UUID bookingId) {
		return bookingAppService.getBooking(bookingId);
	}

	// Lightweight polling endpoint (status + correlationId)
	@GetMapping(value = "/{bookingId}/status", produces = "application/json")
	public BookingStatusResponse getBookingStatus(@PathVariable UUID bookingId) {
		return bookingAppService.getBookingStatus(bookingId);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNotFound(IllegalArgumentException ex) {
		return ex.getMessage();
	}
}