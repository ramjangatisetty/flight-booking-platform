package com.letzautomate.inventory.application;

import com.letzautomate.inventory.infrastructure.persistence.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

	private final InventoryItemRepository items;
	private final InventoryReservationRepository reservations;

	public InventoryService(InventoryItemRepository items, InventoryReservationRepository reservations) {
		this.items = items;
		this.reservations = reservations;
	}

	@Transactional
	public ReserveResult reserve(UUID bookingId, String flightId, String seatClass) {

		// Idempotency: if this booking already processed, return same result
		var existing = reservations.findByBookingId(bookingId);
		if (existing.isPresent()) {
			var r = existing.get();
			return new ReserveResult(r.getReservationId(), r.getStatus());
		}

		var item = items.findByFlightIdAndSeatClass(flightId, seatClass)
				.orElse(null);

		if (item == null || item.getAvailableSeats() <= 0) {
			var rejected = new InventoryReservationEntity();
			rejected.setBookingId(bookingId);
			rejected.setFlightId(flightId);
			rejected.setSeatClass(seatClass);
			rejected.setStatus("REJECTED");
			reservations.save(rejected);

			return new ReserveResult(rejected.getReservationId(), "REJECTED");
		}

		// Reserve seat
		item.setAvailableSeats(item.getAvailableSeats() - 1);
		items.save(item);

		var reserved = new InventoryReservationEntity();
		reserved.setBookingId(bookingId);
		reserved.setFlightId(flightId);
		reserved.setSeatClass(seatClass);
		reserved.setStatus("RESERVED");
		reservations.save(reserved);

		return new ReserveResult(reserved.getReservationId(), "RESERVED");
	}

	public record ReserveResult(UUID reservationId, String status) {}
}
