package com.letzautomate.booking.infrastructure.persistence.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class BookingEntity {

	@Id
	@Column(name = "booking_id", nullable = false)
	private UUID bookingId;

	@Column(name = "correlation_id", nullable = false)
	private UUID correlationId;

	@Column(name = "flight_id", nullable = false)
	private String flightId;

	@Column(name = "seat_class", nullable = false)
	private String seatClass;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "reservation_id")
	private UUID reservationId;

	@Column(name = "payment_id")
	private UUID paymentId;

	@Column(name = "member_id")
	private UUID memberId;

	@Column(name = "loyalty_accrual_status", length = 20)
	private String loyaltyAccrualStatus = "NONE";

	@Column(name = "loyalty_points")
	private Integer loyaltyPoints;

	@Column(name = "loyalty_accrued_at")
	private Instant loyaltyAccruedAt;

	@Column(name = "bag_tag", length = 10)
	private String bagTag;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	// getters/setters
	public UUID getBookingId() { return bookingId; }
	public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

	public UUID getCorrelationId() { return correlationId; }
	public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

	public String getFlightId() { return flightId; }
	public void setFlightId(String flightId) { this.flightId = flightId; }

	public String getSeatClass() { return seatClass; }
	public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public UUID getReservationId() { return reservationId; }
	public void setReservationId(UUID reservationId) { this.reservationId = reservationId; }

	public UUID getPaymentId() { return paymentId; }
	public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

	public UUID getMemberId() { return memberId; }
	public void setMemberId(UUID memberId) { this.memberId = memberId; }

	public String getLoyaltyAccrualStatus() { return loyaltyAccrualStatus; }
	public void setLoyaltyAccrualStatus(String loyaltyAccrualStatus) { this.loyaltyAccrualStatus = loyaltyAccrualStatus; }

	public Integer getLoyaltyPoints() { return loyaltyPoints; }
	public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

	public Instant getLoyaltyAccruedAt() { return loyaltyAccruedAt; }
	public void setLoyaltyAccruedAt(Instant loyaltyAccruedAt) { this.loyaltyAccruedAt = loyaltyAccruedAt; }

	public String getBagTag() { return bagTag; }
	public void setBagTag(String bagTag) { this.bagTag = bagTag; }

	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
