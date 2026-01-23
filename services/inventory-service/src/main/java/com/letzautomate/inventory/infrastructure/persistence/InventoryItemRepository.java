package com.letzautomate.inventory.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {
	Optional<InventoryItemEntity> findByFlightIdAndSeatClass(String flightId, String seatClass);
}
