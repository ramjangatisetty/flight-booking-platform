package com.letzautomate.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "payment_transactions",
		uniqueConstraints = {
				@UniqueConstraint(name = "uq_payment_booking", columnNames = {"booking_id"}),
				@UniqueConstraint(name = "uq_payment_request_event", columnNames = {"request_event_id"})
		}
)
public class PaymentTransactionEntity {

	@Id
	@Column(name = "payment_id", nullable = false)
	private UUID paymentId;

	@Column(name = "booking_id", nullable = false)
	private UUID bookingId;

	@Column(name = "status", nullable = false, length = 20)
	private String status; // SUCCEEDED or FAILED

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 10)
	private String currency;

	@Column(name = "provider", nullable = false, length = 50)
	private String provider;

	@Column(name = "correlation_id")
	private UUID correlationId;

	@Column(name = "request_event_id", nullable = false)
	private String requestEventId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	// Getters and setters
	public UUID getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(UUID paymentId) {
		this.paymentId = paymentId;
	}

	public UUID getBookingId() {
		return bookingId;
	}

	public void setBookingId(UUID bookingId) {
		this.bookingId = bookingId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public UUID getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(UUID correlationId) {
		this.correlationId = correlationId;
	}

	public String getRequestEventId() {
		return requestEventId;
	}

	public void setRequestEventId(String requestEventId) {
		this.requestEventId = requestEventId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
