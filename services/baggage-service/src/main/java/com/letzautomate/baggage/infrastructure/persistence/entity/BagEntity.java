package com.letzautomate.baggage.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bags")
public class BagEntity {

	@Id
	@Column(name = "bag_tag", length = 10)
	private String bagTag;

	@Column(name = "booking_id", nullable = false)
	private UUID bookingId;

	@Column(name = "passenger_id", nullable = false)
	private UUID passengerId;

	@Column(name = "origin", length = 3, nullable = false)
	private String origin;

	@Column(name = "destination", length = 3, nullable = false)
	private String destination;

	@Column(name = "status", length = 20, nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public BagEntity() {}

	public String getBagTag() {
		return bagTag;
	}

	public void setBagTag(String bagTag) {
		this.bagTag = bagTag;
	}

	public UUID getBookingId() {
		return bookingId;
	}

	public void setBookingId(UUID bookingId) {
		this.bookingId = bookingId;
	}

	public UUID getPassengerId() {
		return passengerId;
	}

	public void setPassengerId(UUID passengerId) {
		this.passengerId = passengerId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
