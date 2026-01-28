package com.letzautomate.booking.api.dto;

import java.time.Instant;
import java.util.UUID;

public class LoyaltyAccrualResponse {
	private UUID bookingId;
	private UUID memberId;
	private String loyaltyAccrualStatus;
	private Integer loyaltyPoints;
	private Instant loyaltyAccruedAt;

	public LoyaltyAccrualResponse() {}

	public LoyaltyAccrualResponse(UUID bookingId, UUID memberId, String loyaltyAccrualStatus,
	                               Integer loyaltyPoints, Instant loyaltyAccruedAt) {
		this.bookingId = bookingId;
		this.memberId = memberId;
		this.loyaltyAccrualStatus = loyaltyAccrualStatus;
		this.loyaltyPoints = loyaltyPoints;
		this.loyaltyAccruedAt = loyaltyAccruedAt;
	}

	public UUID getBookingId() { return bookingId; }
	public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

	public UUID getMemberId() { return memberId; }
	public void setMemberId(UUID memberId) { this.memberId = memberId; }

	public String getLoyaltyAccrualStatus() { return loyaltyAccrualStatus; }
	public void setLoyaltyAccrualStatus(String loyaltyAccrualStatus) { this.loyaltyAccrualStatus = loyaltyAccrualStatus; }

	public Integer getLoyaltyPoints() { return loyaltyPoints; }
	public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

	public Instant getLoyaltyAccruedAt() { return loyaltyAccruedAt; }
	public void setLoyaltyAccruedAt(Instant loyaltyAccruedAt) { this.loyaltyAccruedAt = loyaltyAccruedAt; }
}
