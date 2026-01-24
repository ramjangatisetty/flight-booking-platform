package com.letzautomate.booking.application;

import com.letzautomate.booking.api.dto.BookingResponse;
import com.letzautomate.booking.api.dto.BookingStatusResponse;
import com.letzautomate.booking.api.dto.CreateBookingRequest;
import com.letzautomate.booking.domain.Booking;
import com.letzautomate.booking.domain.BookingStatus;
import com.letzautomate.booking.infrastructure.messaging.BookingEventPublisher;
import com.letzautomate.booking.persistence.BookingEntity;
import com.letzautomate.booking.persistence.BookingJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingAppService {

	private final BookingJpaRepository repo;
	private final BookingEventPublisher publisher;

	public BookingAppService(BookingJpaRepository repo, BookingEventPublisher publisher) {
		this.repo = repo;
		this.publisher = publisher;
	}

	// -------------------------
	// Command: Create booking
	// -------------------------
	@Transactional
	public BookingResponse createBooking(CreateBookingRequest req, UUID correlationIdHeader) {

		UUID bookingId = UUID.randomUUID();
		UUID correlationId = (correlationIdHeader != null)
				? correlationIdHeader
				: UUID.randomUUID();

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
		e.setCreatedAt(booking.getCreatedAt());
		e.setUpdatedAt(booking.getUpdatedAt());

		repo.save(e);

		// Publish booking.created.v1
		publisher.publishBookingCreated(booking);

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
				.orElseThrow(() ->
						new IllegalArgumentException("Booking not found: " + bookingId));

		return new BookingStatusResponse(
				e.getBookingId(),
				e.getCorrelationId(),
				e.getStatus(), // already stored as String
				e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null
		);
	}

	// -------------------------
	// Mapper
	// -------------------------
	private BookingResponse toResponse(BookingEntity e) {
		BookingResponse r = new BookingResponse();
		r.setBookingId(e.getBookingId());
		r.setCorrelationId(e.getCorrelationId());
		r.setFlightId(e.getFlightId());
		r.setSeatClass(
				com.letzautomate.booking.api.dto.SeatClass.valueOf(e.getSeatClass())
		);
		r.setAmount(e.getAmount());
		r.setCurrency(e.getCurrency());
		r.setStatus(e.getStatus());
		r.setCreatedAt(e.getCreatedAt());
		r.setUpdatedAt(e.getUpdatedAt());
		return r;
	}
}