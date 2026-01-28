package com.letzautomate.payment.infrastructure.persistence.repository;

import com.letzautomate.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {

	/**
	 * Find payment transaction by request event ID (primary deduplication key)
	 */
	Optional<PaymentTransactionEntity> findByRequestEventId(String requestEventId);

	/**
	 * Find payment transaction by booking ID (secondary lookup)
	 */
	Optional<PaymentTransactionEntity> findByBookingId(UUID bookingId);
}
