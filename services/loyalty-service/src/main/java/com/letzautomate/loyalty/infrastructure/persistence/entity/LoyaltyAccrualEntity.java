package com.letzautomate.loyalty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loyalty_accruals")
public class LoyaltyAccrualEntity {

	@Id
	@Column(name = "booking_id", nullable = false)
	private UUID bookingId;

	@Column(name = "member_id", nullable = false)
	private UUID memberId;

	@Column(name = "points_credited", nullable = false)
	private int pointsCredited;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 10)
	private String currency;

	@Column(name = "correlation_id")
	private UUID correlationId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	// Getters and setters
	public UUID getBookingId() {
		return bookingId;
	}

	public void setBookingId(UUID bookingId) {
		this.bookingId = bookingId;
	}

	public UUID getMemberId() {
		return memberId;
	}

	public void setMemberId(UUID memberId) {
		this.memberId = memberId;
	}

	public int getPointsCredited() {
		return pointsCredited;
	}

	public void setPointsCredited(int pointsCredited) {
		this.pointsCredited = pointsCredited;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public UUID getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(UUID correlationId) {
		this.correlationId = correlationId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
