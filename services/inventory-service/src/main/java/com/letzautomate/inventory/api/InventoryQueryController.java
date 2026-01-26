package com.letzautomate.inventory.api;

import com.letzautomate.inventory.api.dto.InventoryReservationResponse;
import com.letzautomate.inventory.infrastructure.persistence.InventoryReservationEntity;
import com.letzautomate.inventory.infrastructure.persistence.InventoryReservationRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryQueryController {

	private final InventoryReservationRepository reservationRepo;

	public InventoryQueryController(InventoryReservationRepository reservationRepo) {
		this.reservationRepo = reservationRepo;
	}

	@GetMapping("/reservations/{reservationId}")
	public InventoryReservationResponse getByReservationId(@PathVariable UUID reservationId) {
		InventoryReservationEntity e = reservationRepo.findById(reservationId)
				.orElseThrow(() ->
						new IllegalArgumentException("Reservation not found for reservationId: " + reservationId));

		return toResponse(e);
	}

	@GetMapping("/reservations/by-booking/{bookingId}")
	public InventoryReservationResponse getByBookingId(@PathVariable UUID bookingId) {
		InventoryReservationEntity e = reservationRepo.findByBookingId(bookingId)
				.orElseThrow(() ->
						new IllegalArgumentException("Reservation not found for bookingId: " + bookingId));

		return toResponse(e);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNotFound(IllegalArgumentException ex) {
		return ex.getMessage();
	}

	private InventoryReservationResponse toResponse(InventoryReservationEntity e) {
		return new InventoryReservationResponse(
				e.getBookingId(),
				e.getReservationId(),
				e.getStatus(),
				e.getReason(),
				e.getFlightId(),
				e.getSeatClass(),
				e.getCreatedAt()
		);
	}
}