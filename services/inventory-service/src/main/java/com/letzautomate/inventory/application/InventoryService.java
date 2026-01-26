package com.letzautomate.inventory.application;

import com.letzautomate.inventory.infrastructure.persistence.BookingDetailsRepository;
import com.letzautomate.inventory.infrastructure.persistence.InventoryItemEntity;
import com.letzautomate.inventory.infrastructure.persistence.InventoryItemRepository;
import com.letzautomate.inventory.infrastructure.persistence.InventoryReservationEntity;
import com.letzautomate.inventory.infrastructure.persistence.InventoryReservationRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

	private final InventoryItemRepository inventoryRepo;
	private final InventoryReservationRepository reservationRepo;
	private final BookingDetailsRepository bookingDetailsRepo;

	public InventoryService(
			InventoryItemRepository inventoryRepo,
			InventoryReservationRepository reservationRepo,
			BookingDetailsRepository bookingDetailsRepo
	) {
		this.inventoryRepo = inventoryRepo;
		this.reservationRepo = reservationRepo;
		this.bookingDetailsRepo = bookingDetailsRepo;
	}

	public record ReservationResult(String status, UUID reservationId, String reason) {}

	@Transactional
	public ReservationResult reserve(UUID bookingId, String flightId, String seatClass) {

		// Idempotency: if already have a reservation for this booking, return it
		var existing = reservationRepo.findByBookingId(bookingId);
		if (existing.isPresent()) {
			var e = existing.get();
			return new ReservationResult(e.getStatus(), e.getReservationId(), e.getReason());
		}

		var itemOpt = inventoryRepo.findByFlightIdAndSeatClass(flightId, seatClass);
		if (itemOpt.isEmpty()) {
			return persistRejected(bookingId, flightId, seatClass, "NO_SEATS");
		}

		var item = itemOpt.get();

		// No seats -> reject
		if (item.getAvailableSeats() <= 0) {
			return persistRejected(bookingId, flightId, seatClass, "NO_SEATS");
		}

		// Reserve: decrement seats and create reservation row
		item.setAvailableSeats(item.getAvailableSeats() - 1);
		inventoryRepo.save(item);

		InventoryReservationEntity r = new InventoryReservationEntity();
		r.setBookingId(bookingId);
		r.setFlightId(flightId);
		r.setSeatClass(seatClass);
		r.setStatus("RESERVED");
		r.setReason(null);

		r = reservationRepo.save(r);

		return new ReservationResult("RESERVED", r.getReservationId(), null);
	}

	private ReservationResult persistRejected(UUID bookingId, String flightId, String seatClass, String reason) {
		InventoryReservationEntity r = new InventoryReservationEntity();
		r.setBookingId(bookingId);
		r.setFlightId(flightId);
		r.setSeatClass(seatClass);
		r.setStatus("REJECTED");
		r.setReason(reason);

		r = reservationRepo.save(r);
		return new ReservationResult("REJECTED", r.getReservationId(), reason);
	}

	/**
	 * Admin helper: upsert inventory for a flight + seatClass.
	 */
	@Transactional
	public InventoryItemEntity seedInventory(String flightId, String seatClass, int availableSeats) {
		var existing = inventoryRepo.findByFlightIdAndSeatClass(flightId, seatClass);
		if (existing.isPresent()) {
			var item = existing.get();
			item.setAvailableSeats(availableSeats);
			return inventoryRepo.save(item);
		}

		InventoryItemEntity item = new InventoryItemEntity();
		item.setFlightId(flightId);
		item.setSeatClass(seatClass);
		item.setAvailableSeats(availableSeats);
		return inventoryRepo.save(item);
	}

	/**
	 * Admin helper: reset demo state for repeatable runs.
	 */
	@Transactional
	public void resetDemoState() {
		// order matters because of constraints (booking_id unique etc.)
		reservationRepo.deleteAll();
		bookingDetailsRepo.deleteAll();
		inventoryRepo.deleteAll();
	}
}