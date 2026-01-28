package com.letzautomate.inventory.infrastructure.persistence;

import com.letzautomate.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import com.letzautomate.inventory.infrastructure.persistence.repository.InventoryItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class InventorySeeder implements CommandLineRunner {

	private final InventoryItemRepository repo;

	public InventorySeeder(InventoryItemRepository repo) {
		this.repo = repo;
	}

	@Override
	public void run(String... args) {
		seed("AI-112", "ECONOMY", 5);
		seed("AI-112", "BUSINESS", 2);
	}

	private void seed(String flightId, String seatClass, int seats) {
		repo.findByFlightIdAndSeatClass(flightId, seatClass).orElseGet(() -> {
			var e = new InventoryItemEntity();
			e.setFlightId(flightId);
			e.setSeatClass(seatClass);
			e.setAvailableSeats(seats);
			return repo.save(e);
		});
	}
}
