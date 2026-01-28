package com.letzautomate.booking.application;

import com.letzautomate.booking.infrastructure.loyalty.LoyaltySoapClient;
import com.letzautomate.booking.infrastructure.persistence.entity.BookingEntity;
import com.letzautomate.booking.infrastructure.persistence.repository.BookingJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class LoyaltyAccrualService {

	private final BookingJpaRepository bookingRepository;
	private final LoyaltySoapClient loyaltySoapClient;
	private final DataSource dataSource;

	public LoyaltyAccrualService(BookingJpaRepository bookingRepository, LoyaltySoapClient loyaltySoapClient, DataSource dataSource) {
		this.bookingRepository = bookingRepository;
		this.loyaltySoapClient = loyaltySoapClient;
		this.dataSource = dataSource;
	}

	/**
	 * Accrue loyalty points for a booking.
	 * Uses raw JDBC to update only loyalty-specific fields to avoid overwriting bag_tag.
	 */
	public void accruePointsForBooking(UUID bookingId) {
		BookingEntity booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		if (!"CONFIRMED".equals(booking.getStatus())) {
			System.out.println("Skipping loyalty accrual for non-confirmed booking. bookingId=" + bookingId);
			return;
		}

		if (booking.getMemberId() == null) {
			System.out.println("Skipping loyalty accrual - no member ID. bookingId=" + bookingId);
			updateLoyaltyStatus(bookingId, "NONE", null, null);
			return;
		}

		if ("SUCCEEDED".equals(booking.getLoyaltyAccrualStatus())) {
			System.out.println("Loyalty points already accrued. bookingId=" + bookingId);
			return;
		}

		updateLoyaltyStatus(bookingId, "REQUESTED", null, null);

		try {
			System.out.println("Accruing loyalty points. bookingId=" + bookingId);

			LoyaltySoapClient.AccruePointsResponse response = loyaltySoapClient.accruePoints(
				booking.getMemberId(),
				bookingId,
				booking.getAmount(),
				booking.getCurrency(),
				booking.getCorrelationId()
			);

			updateLoyaltyStatus(bookingId, "SUCCEEDED", response.pointsCredited, Instant.now());
			System.out.println("Loyalty points accrued successfully. bookingId=" + bookingId);

		} catch (LoyaltySoapClient.LoyaltyServiceException e) {
			System.err.println("Loyalty accrual failed. bookingId=" + bookingId);
			updateLoyaltyStatus(bookingId, "FAILED", null, null);
		} catch (Exception e) {
			System.err.println("Unexpected error during loyalty accrual. bookingId=" + bookingId);
			updateLoyaltyStatus(bookingId, "FAILED", null, null);
		}
	}

	/**
	 * Update only loyalty-specific fields using direct JDBC connection.
	 */
	private void updateLoyaltyStatus(UUID bookingId, String status, Integer points, Instant accruedAt) {
		String url = "jdbc:postgresql://localhost:5433/bookingdb";
		String user = "booking";
		String password = "booking";
		String sql = "UPDATE bookings SET loyalty_accrual_status = ?, loyalty_points = ?, loyalty_accrued_at = ? WHERE booking_id = ?::uuid";
		
		try (Connection conn = java.sql.DriverManager.getConnection(url, user, password)) {
			conn.setAutoCommit(true);
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, status);
				if (points != null) {
					ps.setInt(2, points);
				} else {
					ps.setNull(2, java.sql.Types.INTEGER);
				}
				if (accruedAt != null) {
					ps.setTimestamp(3, Timestamp.from(accruedAt));
				} else {
					ps.setNull(3, java.sql.Types.TIMESTAMP);
				}
				ps.setString(4, bookingId.toString());
				int updated = ps.executeUpdate();
				System.out.println(">>> Loyalty status updated: " + status + ", rows=" + updated);
			}
		} catch (Exception e) {
			System.err.println("Failed to update loyalty status: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public LoyaltyAccrualInfo getLoyaltyAccrualInfo(UUID bookingId) {
		BookingEntity booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

		return new LoyaltyAccrualInfo(
			booking.getBookingId(),
			booking.getMemberId(),
			booking.getLoyaltyAccrualStatus(),
			booking.getLoyaltyPoints(),
			booking.getLoyaltyAccruedAt()
		);
	}

	public static class LoyaltyAccrualInfo {
		public final UUID bookingId;
		public final UUID memberId;
		public final String loyaltyAccrualStatus;
		public final Integer loyaltyPoints;
		public final Instant loyaltyAccruedAt;

		public LoyaltyAccrualInfo(UUID bookingId, UUID memberId, String loyaltyAccrualStatus,
                           Integer loyaltyPoints, Instant loyaltyAccruedAt) {
			this.bookingId = bookingId;
			this.memberId = memberId;
			this.loyaltyAccrualStatus = loyaltyAccrualStatus;
			this.loyaltyPoints = loyaltyPoints;
			this.loyaltyAccruedAt = loyaltyAccruedAt;
		}
	}
}
