package com.letzautomate.baggage.infrastructure.persistence.repository;

import com.letzautomate.baggage.infrastructure.persistence.entity.BagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BagRepository extends JpaRepository<BagEntity, String> {
	Optional<BagEntity> findByBookingId(UUID bookingId);
}
