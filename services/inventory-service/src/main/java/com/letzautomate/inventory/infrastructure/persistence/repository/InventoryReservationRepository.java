package com.letzautomate.inventory.infrastructure.persistence.repository;

import com.letzautomate.inventory.infrastructure.persistence.entity.InventoryReservationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservationEntity, UUID> {
	Optional<InventoryReservationEntity> findByBookingId(UUID bookingId);
}
