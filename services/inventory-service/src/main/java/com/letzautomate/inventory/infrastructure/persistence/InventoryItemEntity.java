package com.letzautomate.inventory.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
		name = "inventory_items",
		uniqueConstraints = @UniqueConstraint(name = "uq_inventory_flight_class",
				columnNames = {"flight_id", "seat_class"})
)
public class InventoryItemEntity {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(name = "flight_id", nullable = false)
	private String flightId;

	@Column(name = "seat_class", nullable = false)
	private String seatClass;

	@Column(name = "available_seats", nullable = false)
	private int availableSeats;

	@Version
	private long version;

	public UUID getId() { return id; }

	public String getFlightId() { return flightId; }
	public void setFlightId(String flightId) { this.flightId = flightId; }

	public String getSeatClass() { return seatClass; }
	public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

	public int getAvailableSeats() { return availableSeats; }
	public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

	public long getVersion() { return version; }
}
