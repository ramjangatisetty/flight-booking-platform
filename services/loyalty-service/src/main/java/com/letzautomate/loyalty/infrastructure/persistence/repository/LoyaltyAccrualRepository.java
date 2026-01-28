package com.letzautomate.loyalty.infrastructure.persistence.repository;

import com.letzautomate.loyalty.infrastructure.persistence.entity.LoyaltyAccrualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoyaltyAccrualRepository extends JpaRepository<LoyaltyAccrualEntity, UUID> {
}
