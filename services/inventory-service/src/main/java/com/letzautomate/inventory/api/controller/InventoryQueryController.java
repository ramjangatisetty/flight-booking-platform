package com.letzautomate.inventory.api.controller;

import com.letzautomate.inventory.api.dto.InventoryReservationResponse;
import com.letzautomate.inventory.api.exception.ReservationNotFoundException;
import com.letzautomate.inventory.infrastructure.persistence.entity.InventoryReservationEntity;
import com.letzautomate.inventory.infrastructure.persistence.repository.InventoryReservationRepository;
import java.util.UUID;
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
				.orElseThrow(() -> ReservationNotFoundException.forReservationId(reservationId));

		return toResponse(e);
	}

	@GetMapping("/reservations/by-booking/{bookingId}")
	public InventoryReservationResponse getByBookingId(@PathVariable UUID bookingId) {
		InventoryReservationEntity e = reservationRepo.findByBookingId(bookingId)
				.orElseThrow(() -> new ReservationNotFoundException(bookingId));

		return toResponse(e);
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
