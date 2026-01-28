package com.letzautomate.loyalty.infrastructure.persistence.repository;

import com.letzautomate.loyalty.infrastructure.persistence.entity.LoyaltyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyMemberRepository extends JpaRepository<LoyaltyMemberEntity, UUID> {

	Optional<LoyaltyMemberEntity> findByEmail(String email);
}
