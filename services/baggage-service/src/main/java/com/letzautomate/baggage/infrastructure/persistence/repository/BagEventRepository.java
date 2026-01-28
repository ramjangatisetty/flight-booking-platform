package com.letzautomate.baggage.infrastructure.persistence.repository;

import com.letzautomate.baggage.infrastructure.persistence.entity.BagEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BagEventRepository extends JpaRepository<BagEventEntity, Long> {
	List<BagEventEntity> findByBagTagOrderByOccurredAtAsc(String bagTag);
}
