package com.letzautomate.inventory.application;

import com.letzautomate.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import com.letzautomate.inventory.infrastructure.persistence.entity.InventoryReservationEntity;
import com.letzautomate.inventory.infrastructure.persistence.repository.BookingDetailsRepository;
import com.letzautomate.inventory.infrastructure.persistence.repository.InventoryItemRepository;
import com.letzautomate.inventory.infrastructure.persistence.repository.InventoryReservationRepository;
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
	 * - RESERVED: seats decremented and reservation entity created
	 * - REJECTED: NO reservation entity created, only audit log
	 * 
	 * Reservation table rows only exist for RESERVED/RELEASED status.
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
			// No inventory item found - reject without creating reservation entity
			System.out.println("⚠ No inventory item found for " + flightId + "/" + seatClass + 
			                   " - rejecting booking " + bookingId + " (reason: FLIGHT_NOT_FOUND)");
			return new ReserveResult("REJECTED", null, "FLIGHT_NOT_FOUND");
		}

		if (item.getAvailableSeats() <= 0) {
			// No seats available - reject without creating reservation entity
			System.out.println("⚠ No seats available for " + flightId + "/" + seatClass + 
			                   " - rejecting booking " + bookingId + " (reason: NO_SEATS)");
			return new ReserveResult("REJECTED", null, "NO_SEATS");
		}

		// SUCCESS: decrement seats (optimistic lock via @Version)
		item.setAvailableSeats(item.getAvailableSeats() - 1);
		itemRepo.save(item);

		// Create reservation entity ONLY for successful reservations
		InventoryReservationEntity res = new InventoryReservationEntity();
		res.setBookingId(bookingId);
		res.setFlightId(flightId);
		res.setSeatClass(seatClass);
		res.setStatus("RESERVED");
		res.setReason(null);

		InventoryReservationEntity saved = reservationRepo.save(res);
		
		System.out.println("✓ Created reservation " + saved.getReservationId() + 
		                   " for booking " + bookingId + " (" + flightId + "/" + seatClass + ")");
		
		return new ReserveResult(saved.getStatus(), saved.getReservationId(), saved.getReason());
	}

	/**
	 * Explicit rejection path (deprecated - no longer creates reservation entities).
	 * Rejections are now handled inline in reserve() without persisting.
	 * This method is kept for backward compatibility but does nothing.
	 */
	@Deprecated
	@Transactional
	public ReserveResult reject(UUID bookingId, String flightId, String seatClass, String reason) {
		
		System.out.println("⚠ reject() called for booking " + bookingId + 
		                   " - this method is deprecated and does not create reservation entities");

		// Idempotency: if reservation already exists for bookingId, return it
		Optional<InventoryReservationEntity> existing = reservationRepo.findByBookingId(bookingId);
		if (existing.isPresent()) {
			InventoryReservationEntity e = existing.get();
			return new ReserveResult(e.getStatus(), e.getReservationId(), e.getReason());
		}

		// No reservation entity created for rejections
		return new ReserveResult("REJECTED", null, reason);
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

	/**
	 * Release a reservation (compensation for payment failure).
	 * - Looks up reservation by reservationId or bookingId
	 * - Marks it as RELEASED
	 * - Increments seat availability
	 * - Idempotent: if already released, does nothing
	 */
	@Transactional
	public ReleaseResult release(UUID bookingId, UUID reservationId) {

		// Try to find reservation by reservationId first, then by bookingId
		Optional<InventoryReservationEntity> reservationOpt = Optional.empty();
		
		if (reservationId != null) {
			reservationOpt = reservationRepo.findById(reservationId);
		}
		
		if (reservationOpt.isEmpty() && bookingId != null) {
			reservationOpt = reservationRepo.findByBookingId(bookingId);
		}

		if (reservationOpt.isEmpty()) {
			// No reservation found - might have been deleted or never created (rejection case)
			System.out.println("⚠ No reservation found for bookingId: " + bookingId + 
			                   ", reservationId: " + reservationId + 
			                   " - likely a rejection, treating as already released");
			return new ReleaseResult("RELEASED", reservationId, "NOT_FOUND");
		}

		InventoryReservationEntity reservation = reservationOpt.get();

		// Idempotency: if already released, return success
		if ("RELEASED".equalsIgnoreCase(reservation.getStatus())) {
			System.out.println("✓ Reservation " + reservation.getReservationId() + 
			                   " already RELEASED - idempotent operation");
			return new ReleaseResult("RELEASED", reservation.getReservationId(), "ALREADY_RELEASED");
		}

		// Only release if it was RESERVED
		if (!"RESERVED".equalsIgnoreCase(reservation.getStatus())) {
			System.out.println("⚠ Reservation " + reservation.getReservationId() + 
			                   " has status " + reservation.getStatus() + " - not releasing");
			return new ReleaseResult(reservation.getStatus(), reservation.getReservationId(), "NOT_RESERVED");
		}

		// Increment seat availability
		InventoryItemEntity item = itemRepo.findByFlightIdAndSeatClass(
				reservation.getFlightId(), 
				reservation.getSeatClass()
		).orElse(null);

		if (item != null) {
			item.setAvailableSeats(item.getAvailableSeats() + 1);
			itemRepo.save(item);
			System.out.println("✓ Incremented seat availability for " + reservation.getFlightId() + 
			                   "/" + reservation.getSeatClass() + " - now " + item.getAvailableSeats());
		} else {
			System.out.println("⚠ Inventory item not found for " + reservation.getFlightId() + 
			                   "/" + reservation.getSeatClass() + " - cannot increment seats");
		}

		// Mark reservation as RELEASED
		reservation.setStatus("RELEASED");
		reservationRepo.save(reservation);

		System.out.println("✓ Released reservation " + reservation.getReservationId() + 
		                   " for booking " + bookingId);

		return new ReleaseResult("RELEASED", reservation.getReservationId(), null);
	}

	public record ReleaseResult(String status, UUID reservationId, String reason) {}
}
