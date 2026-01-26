package com.letzautomate.inventory.application;

import com.letzautomate.inventory.infrastructure.persistence.*;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

	private final InventoryItemRepository itemRepo;
	private final InventoryReservationRepository reservationRepo;
	private final BookingDetailsRepository bookingDetailsRepo;

	public InventoryService(
			InventoryItemRepository itemRepo,
			InventoryReservationRepository reservationRepo,
			BookingDetailsRepository bookingDetailsRepo
	) {
		this.itemRepo = itemRepo;
		this.reservationRepo = reservationRepo;
		this.bookingDetailsRepo = bookingDetailsRepo;
	}

	/**
	 * Reserve a seat for a booking.
	 * - RESERVED: seats decremented and reservation stored
	 * - REJECTED: reservation stored with reason (e.g. NO_SEATS)
	 */
	@Transactional
	public ReserveResult reserve(UUID bookingId, String flightId, String seatClass) {

		// Idempotency: if reservation already exists for bookingId, return it as-is
		Optional<InventoryReservationEntity> existing = reservationRepo.findByBookingId(bookingId);
		if (existing.isPresent()) {
			InventoryReservationEntity e = existing.get();
			return new ReserveResult(e.getStatus(), e.getReservationId(), e.getReason());
		}

		InventoryItemEntity item = itemRepo.findByFlightIdAndSeatClass(flightId, seatClass)
				.orElse(null);

		if (item == null) {
			// treat missing inventory row as no seats (demo-friendly)
			InventoryReservationEntity rej = new InventoryReservationEntity();
			rej.setBookingId(bookingId);
			rej.setFlightId(flightId);
			rej.setSeatClass(seatClass);
			rej.setStatus("REJECTED");
			rej.setReason("NO_SEATS");
			InventoryReservationEntity saved = reservationRepo.save(rej);
			return new ReserveResult(saved.getStatus(), saved.getReservationId(), saved.getReason());
		}

		if (item.getAvailableSeats() <= 0) {
			InventoryReservationEntity rej = new InventoryReservationEntity();
			rej.setBookingId(bookingId);
			rej.setFlightId(flightId);
			rej.setSeatClass(seatClass);
			rej.setStatus("REJECTED");
			rej.setReason("NO_SEATS");
			InventoryReservationEntity saved = reservationRepo.save(rej);
			return new ReserveResult(saved.getStatus(), saved.getReservationId(), saved.getReason());
		}

		// decrement seats (optimistic lock via @Version)
		item.setAvailableSeats(item.getAvailableSeats() - 1);
		itemRepo.save(item);

		InventoryReservationEntity res = new InventoryReservationEntity();
		res.setBookingId(bookingId);
		res.setFlightId(flightId);
		res.setSeatClass(seatClass);
		res.setStatus("RESERVED");
		res.setReason(null);

		InventoryReservationEntity saved = reservationRepo.save(res);
		return new ReserveResult(saved.getStatus(), saved.getReservationId(), saved.getReason());
	}

	/**
	 * Explicit rejection path (used for payment.failed or other upstream failures).
	 * This should NOT decrement inventory seats.
	 */
	@Transactional
	public ReserveResult reject(UUID bookingId, String flightId, String seatClass, String reason) {

		// Idempotency: if reservation already exists for bookingId, don’t create duplicates
		Optional<InventoryReservationEntity> existing = reservationRepo.findByBookingId(bookingId);
		if (existing.isPresent()) {
			InventoryReservationEntity e = existing.get();

			// If it was RESERVED already, don’t downgrade it in MVP (keep stable)
			// If you want compensation later, that’s a different feature.
			if ("RESERVED".equalsIgnoreCase(e.getStatus())) {
				return new ReserveResult(e.getStatus(), e.getReservationId(), e.getReason());
			}

			e.setStatus("REJECTED");
			e.setReason(reason);
			InventoryReservationEntity saved = reservationRepo.save(e);
			return new ReserveResult(saved.getStatus(), saved.getReservationId(), saved.getReason());
		}

		InventoryReservationEntity rej = new InventoryReservationEntity();
		rej.setBookingId(bookingId);
		rej.setFlightId(flightId);
		rej.setSeatClass(seatClass);
		rej.setStatus("REJECTED");
		rej.setReason(reason);

		InventoryReservationEntity saved = reservationRepo.save(rej);
		return new ReserveResult(saved.getStatus(), saved.getReservationId(), saved.getReason());
	}

	// -----------------------------
	// Demo admin helpers
	// -----------------------------

	@Transactional
	public InventoryItemEntity seedInventory(String flightId, String seatClass, int availableSeats) {
		InventoryItemEntity item = itemRepo.findByFlightIdAndSeatClass(flightId, seatClass)
				.orElseGet(InventoryItemEntity::new);

		item.setFlightId(flightId);
		item.setSeatClass(seatClass);
		item.setAvailableSeats(availableSeats);
		return itemRepo.save(item);
	}

	@Transactional
	public void resetDemoState() {
		// order matters because of constraints in some DB setups
		reservationRepo.deleteAll();
		itemRepo.deleteAll();
		bookingDetailsRepo.deleteAll();
	}

	/**
	 * ReserveResult now supports reason() -> fixes your compile error #1.
	 */
	public record ReserveResult(String status, UUID reservationId, String reason) {}
}