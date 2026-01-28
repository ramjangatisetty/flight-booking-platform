package com.letzautomate.booking.application;

import com.letzautomate.booking.api.dto.BookingResponse;
import com.letzautomate.booking.api.dto.BookingStatusResponse;
import com.letzautomate.booking.api.dto.CreateBookingRequest;
import com.letzautomate.booking.domain.model.Booking;
import com.letzautomate.booking.domain.model.BookingStatus;
import com.letzautomate.booking.infrastructure.messaging.producer.BookingEventPublisher;
import com.letzautomate.booking.infrastructure.persistence.entity.BookingEntity;
import com.letzautomate.booking.infrastructure.persistence.repository.BookingJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingAppService {

	private final BookingJpaRepository repo;
	private final BookingEventPublisher publisher;
	private final LoyaltyAccrualService loyaltyAccrualService;
	private final DataSource dataSource;
	
	@PersistenceContext
	private EntityManager entityManager;

	public BookingAppService(BookingJpaRepository repo, BookingEventPublisher publisher, 
	                          LoyaltyAccrualService loyaltyAccrualService,
	                          DataSource dataSource) {
		this.repo = repo;
		this.publisher = publisher;
		this.loyaltyAccrualService = loyaltyAccrualService;
		this.dataSource = dataSource;
	}

	// -------------------------
	// Command: Create booking
	// -------------------------
	@Transactional
	public BookingResponse createBooking(CreateBookingRequest req, UUID correlationIdHeader) {

		UUID bookingId = UUID.randomUUID();
		UUID correlationId = (correlationIdHeader != null) ? correlationIdHeader : UUID.randomUUID();
		Instant now = Instant.now();

		Booking booking = new Booking(
				bookingId,
				correlationId,
				req.getFlightId(),
				req.getSeatClass().name(),
				req.getAmount(),
				"USD",
				BookingStatus.PENDING_PAYMENT,
				now,
				now
		);

		BookingEntity e = new BookingEntity();
		e.setBookingId(booking.getBookingId());
		e.setCorrelationId(booking.getCorrelationId());
		e.setFlightId(booking.getFlightId());
		e.setSeatClass(booking.getSeatClass());
		e.setAmount(booking.getAmount());
		e.setCurrency(booking.getCurrency());
		e.setStatus(booking.getStatus().name());
		e.setMemberId(req.getMemberId()); // Persist loyalty member ID if provided
		e.setCreatedAt(booking.getCreatedAt());
		e.setUpdatedAt(booking.getUpdatedAt());

		repo.save(e);

		// Publish inventory.reserve.requested.v1 (after save)
		publisher.publishInventoryReserveRequested(booking);

		return toResponse(e);
	}

	// -------------------------
	// Query: Full booking
	// -------------------------
	@Transactional(readOnly = true)
	public BookingResponse getBooking(UUID bookingId) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
		return toResponse(e);
	}

	// -------------------------
	// Query: Minimal status (polling)
	// -------------------------
	@Transactional(readOnly = true)
	public BookingStatusResponse getBookingStatus(UUID bookingId) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		return new BookingStatusResponse(
				e.getBookingId(),
				e.getCorrelationId(),
				e.getStatus(),
				e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null
		);
	}

	// -------------------------
	// Query: Loyalty accrual information
	// -------------------------
	@Transactional(readOnly = true)
	public com.letzautomate.booking.api.dto.LoyaltyAccrualResponse getLoyaltyAccrual(UUID bookingId) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		return new com.letzautomate.booking.api.dto.LoyaltyAccrualResponse(
				e.getBookingId(),
				e.getMemberId(),
				e.getLoyaltyAccrualStatus(),
				e.getLoyaltyPoints(),
				e.getLoyaltyAccruedAt()
		);
	}

	// -------------------------
	// Event handlers from inventory-service
	// -------------------------

	/**
	 * inventory.reserved.v1 handler
	 * Now triggers payment.requested.v1 publication
	 */
	@Transactional
	public void markInventoryReserved(UUID bookingId, UUID correlationIdFromEvent, UUID reservationId) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		// idempotency / duplicates / late events
		if (isTerminal(e.getStatus())) return;

		// Keep correlationId stable; only set if null (optional)
		if (e.getCorrelationId() == null && correlationIdFromEvent != null) {
			e.setCorrelationId(correlationIdFromEvent);
		}

		// Store reservationId from inventory service
		e.setReservationId(reservationId);
		
		// Log for verification
		System.out.println("✓ Stored reservationId: " + reservationId + " for booking: " + bookingId);

		// Update status to indicate inventory is reserved, awaiting payment
		e.setStatus("PENDING_PAYMENT");
		e.setUpdatedAt(Instant.now());
		repo.save(e);

		// Publish payment.requested.v1
		publisher.publishPaymentRequested(
				bookingId,
				e.getCorrelationId(),
				reservationId,
				e.getFlightId(),
				e.getSeatClass(),
				e.getAmount(),
				e.getCurrency()
		);
	}

	/**
	 * inventory.rejected.v1 handler
	 * Immediately rejects the booking and publishes booking.rejected.v1
	 */
	@Transactional
	public void markInventoryRejected(UUID bookingId, UUID correlationIdFromEvent, String reason) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		if (isTerminal(e.getStatus())) return;

		if (e.getCorrelationId() == null && correlationIdFromEvent != null) {
			e.setCorrelationId(correlationIdFromEvent);
		}

		e.setStatus("REJECTED");
		e.setUpdatedAt(Instant.now());
		repo.save(e);

		// Publish booking.rejected.v1 with full booking details
		// reservationId and paymentId are null since rejection happened at inventory stage
		publisher.publishBookingRejected(
				bookingId,
				e.getCorrelationId(),
				null, // reservationId - null (rejected at inventory stage)
				null, // paymentId - null (never reached payment)
				e.getFlightId(),
				e.getSeatClass(),
				e.getAmount(),
				e.getCurrency(),
				reason
		);
	}

	// -------------------------
	// Event handlers from payment-service
	// -------------------------

	/**
	 * payment.succeeded.v1 handler
	 * Confirms the booking and publishes booking.confirmed.v1
	 * 
	 * NOTE: This method does NOT call loyalty accrual directly to avoid transaction issues.
	 * Loyalty accrual is triggered separately by the caller.
	 */
	@Transactional
	public void markPaymentSucceeded(UUID bookingId, UUID correlationIdFromEvent, UUID paymentId) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		if (isTerminal(e.getStatus())) return;

		if (e.getCorrelationId() == null && correlationIdFromEvent != null) {
			e.setCorrelationId(correlationIdFromEvent);
		}

		// Store paymentId
		e.setPaymentId(paymentId);

		e.setStatus("CONFIRMED");
		e.setUpdatedAt(Instant.now());
		repo.save(e);

		// Assertion: reservationId must be non-null before publishing booking.confirmed
		if (e.getReservationId() == null) {
			throw new IllegalStateException(
					"Cannot confirm booking " + bookingId + " without reservationId. " +
					"This indicates inventory.reserved.v1 was not processed correctly."
			);
		}

		// Log for verification
		System.out.println("✓ Publishing booking.confirmed.v1 with reservationId: " + e.getReservationId() + 
		                   " and paymentId: " + paymentId + " for booking: " + bookingId);

		// Publish booking.confirmed.v1 with stored reservationId
		publisher.publishBookingConfirmed(
				bookingId,
				e.getCorrelationId(),
				e.getReservationId(), // Now using stored reservationId
				paymentId,
				e.getFlightId(),
				e.getSeatClass(),
				e.getAmount(),
				e.getCurrency()
		);
		
		// NOTE: Loyalty accrual is NOT called here anymore.
		// It's called separately AFTER this transaction commits to avoid race conditions with bag_tag updates.
	}
	
	/**
	 * Trigger loyalty accrual for a booking.
	 * This should be called AFTER the booking confirmation transaction has committed.
	 */
	public void triggerLoyaltyAccrual(UUID bookingId) {
		try {
			loyaltyAccrualService.accruePointsForBooking(bookingId);
		} catch (Exception ex) {
			// Log but do not propagate - booking confirmation must succeed regardless of loyalty accrual
			System.err.println("⚠ Loyalty accrual failed for booking " + bookingId + ": " + ex.getMessage());
		}
	}

	/**
	 * payment.failed.v1 handler
	 * 1. First publishes inventory.release.requested.v1 for compensation (if reservationId exists)
	 * 2. Then transitions booking to REJECTED and persists
	 * 3. Finally publishes booking.rejected.v1
	 * 
	 * This ordering ensures compensation starts immediately before booking rejection is finalized.
	 */
	@Transactional
	public void markPaymentFailed(UUID bookingId, UUID correlationIdFromEvent, String reasonCode) {
		BookingEntity e = repo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		if (isTerminal(e.getStatus())) return;

		if (e.getCorrelationId() == null && correlationIdFromEvent != null) {
			e.setCorrelationId(correlationIdFromEvent);
		}

		// STEP 1: Compensation - Publish inventory.release.requested.v1 FIRST (if we have a reservationId)
		if (e.getReservationId() != null) {
			System.out.println("✓ [1/3] Publishing inventory.release.requested.v1 for compensation - " +
			                   "reservationId: " + e.getReservationId() + ", booking: " + bookingId);
			
			publisher.publishInventoryReleaseRequested(
					bookingId,
					e.getCorrelationId(),
					e.getReservationId(),
					"PAYMENT_FAILED"
			);
		} else {
			System.out.println("⚠ [1/3] No reservationId found for booking " + bookingId + 
			                   " - skipping inventory release (inventory was never reserved)");
		}

		// STEP 2: Update booking status to REJECTED and persist
		System.out.println("✓ [2/3] Transitioning booking " + bookingId + " to REJECTED status");
		e.setStatus("REJECTED");
		e.setUpdatedAt(Instant.now());
		repo.save(e);

		// STEP 3: Publish booking.rejected.v1 with full booking details and payment failure reason
		// reservationId is present (inventory was reserved), paymentId is null (payment failed)
		System.out.println("✓ [3/3] Publishing booking.rejected.v1 for booking: " + bookingId + 
		                   ", reason: " + reasonCode);
		publisher.publishBookingRejected(
				bookingId,
				e.getCorrelationId(),
				e.getReservationId(), // Present (inventory was reserved)
				null, // paymentId - null (payment failed)
				e.getFlightId(),
				e.getSeatClass(),
				e.getAmount(),
				e.getCurrency(),
				reasonCode
		);
	}

	/**
	 * Update booking with bagTag from baggage service.
	 * Called when baggage.checked_in.v1 event is received.
	 * 
	 * Uses a direct JDBC connection (bypassing connection pool) to ensure commit.
	 */
	public void updateBagTag(UUID bookingId, String bagTag) {
		System.out.println(">>> updateBagTag called: bookingId=" + bookingId + ", bagTag=" + bagTag);
		
		String url = "jdbc:postgresql://localhost:5433/bookingdb";
		String user = "booking";
		String password = "booking";
		String sql = "UPDATE bookings SET bag_tag = ?, updated_at = ? WHERE booking_id = ?::uuid";
		
		try (Connection conn = java.sql.DriverManager.getConnection(url, user, password)) {
			conn.setAutoCommit(true);
			System.out.println(">>> Got direct JDBC connection (not from pool)");
			
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, bagTag);
				ps.setTimestamp(2, Timestamp.from(Instant.now()));
				ps.setString(3, bookingId.toString());
				
				int rowsUpdated = ps.executeUpdate();
				System.out.println(">>> Direct JDBC UPDATE executed: rowsUpdated=" + rowsUpdated);
				
				if (rowsUpdated > 0) {
					System.out.println(">>> SUCCESS for bookingId=" + bookingId + ", bagTag=" + bagTag);
				} else {
					System.out.println(">>> WARNING: No rows updated. Booking may not exist: " + bookingId);
				}
			}
		} catch (Exception e) {
			System.out.println(">>> JDBC ERROR: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to update bagTag", e);
		}
	}

	private boolean isTerminal(String status) {
		if (status == null) return false;
		return "CONFIRMED".equalsIgnoreCase(status)
				|| "REJECTED".equalsIgnoreCase(status)
				|| "CANCELLED".equalsIgnoreCase(status);
	}

	// -------------------------
	// Mapper
	// -------------------------
	private BookingResponse toResponse(BookingEntity e) {
		BookingResponse r = new BookingResponse();
		r.setBookingId(e.getBookingId());
		r.setCorrelationId(e.getCorrelationId());
		r.setFlightId(e.getFlightId());
		r.setSeatClass(com.letzautomate.booking.api.dto.SeatClass.valueOf(e.getSeatClass()));
		r.setAmount(e.getAmount());
		r.setCurrency(e.getCurrency());
		r.setStatus(e.getStatus());
		r.setCreatedAt(e.getCreatedAt());
		r.setUpdatedAt(e.getUpdatedAt());
		r.setMemberId(e.getMemberId());
		r.setLoyaltyAccrualStatus(e.getLoyaltyAccrualStatus());
		r.setLoyaltyPoints(e.getLoyaltyPoints());
		r.setBagTag(e.getBagTag());
		return r;
	}
}
