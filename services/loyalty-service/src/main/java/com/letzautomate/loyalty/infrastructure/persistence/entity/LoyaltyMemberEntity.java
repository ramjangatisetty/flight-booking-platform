package com.letzautomate.loyalty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "loyalty_members",
		uniqueConstraints = @UniqueConstraint(name = "uq_loyalty_email", columnNames = {"email"})
)
public class LoyaltyMemberEntity {

	@Id
	@Column(name = "member_id", nullable = false)
	private UUID memberId;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "email", nullable = false, length = 255)
	private String email;

	@Column(name = "tier", nullable = false, length = 20)
	private String tier;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "points_balance", nullable = false)
	private int pointsBalance;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	// Getters and setters
	public UUID getMemberId() {
		return memberId;
	}

	public void setMemberId(UUID memberId) {
		this.memberId = memberId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getPointsBalance() {
		return pointsBalance;
	}

	public void setPointsBalance(int pointsBalance) {
		this.pointsBalance = pointsBalance;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
