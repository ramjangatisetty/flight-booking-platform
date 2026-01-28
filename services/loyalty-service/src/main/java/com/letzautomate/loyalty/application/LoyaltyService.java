package com.letzautomate.loyalty.application;

import com.letzautomate.loyalty.infrastructure.persistence.entity.LoyaltyAccrualEntity;
import com.letzautomate.loyalty.infrastructure.persistence.entity.LoyaltyMemberEntity;
import com.letzautomate.loyalty.infrastructure.persistence.repository.LoyaltyAccrualRepository;
import com.letzautomate.loyalty.infrastructure.persistence.repository.LoyaltyMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoyaltyService {

	private static final Logger log = LoggerFactory.getLogger(LoyaltyService.class);

	private final LoyaltyMemberRepository memberRepository;
	private final LoyaltyAccrualRepository accrualRepository;

	public LoyaltyService(LoyaltyMemberRepository memberRepository, LoyaltyAccrualRepository accrualRepository) {
		this.memberRepository = memberRepository;
		this.accrualRepository = accrualRepository;
	}

	@Transactional
	public LoyaltyMemberEntity enrollMember(String firstName, String lastName, String email) {
		// Check if member already exists
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("Member with email " + email + " already exists");
		}

		LoyaltyMemberEntity member = new LoyaltyMemberEntity();
		member.setMemberId(UUID.randomUUID());
		member.setFirstName(firstName);
		member.setLastName(lastName);
		member.setEmail(email);
		member.setTier("BASIC"); // New members start at BASIC tier
		member.setStatus("ACTIVE");
		member.setPointsBalance(0);
		member.setCreatedAt(Instant.now());

		return memberRepository.save(member);
	}

	@Transactional(readOnly = true)
	public LoyaltyMemberEntity getMemberStatus(UUID memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));
	}

	@Transactional
	public LoyaltyMemberEntity seedMember(String firstName, String lastName, String email, String tier, int pointsBalance) {
		// Check if member already exists
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("Member with email " + email + " already exists");
		}

		LoyaltyMemberEntity member = new LoyaltyMemberEntity();
		member.setMemberId(UUID.randomUUID());
		member.setFirstName(firstName);
		member.setLastName(lastName);
		member.setEmail(email);
		member.setTier(tier);
		member.setStatus("ACTIVE");
		member.setPointsBalance(pointsBalance);
		member.setCreatedAt(Instant.now());

		return memberRepository.save(member);
	}

	@Transactional
	public void resetDemoState() {
		memberRepository.deleteAll();
	}

	@Transactional
	public AccrualResult accruePoints(UUID memberId, UUID bookingId, BigDecimal amount, String currency, UUID correlationId) {
		log.info("Accruing points for booking. memberId={}, bookingId={}, amount={}, currency={}, correlationId={}",
				memberId, bookingId, amount, currency, correlationId);

		// IDEMPOTENCY CHECK: Check if points already accrued for this booking
		Optional<LoyaltyAccrualEntity> existing = accrualRepository.findById(bookingId);
		if (existing.isPresent()) {
			LoyaltyAccrualEntity accrual = existing.get();
			LoyaltyMemberEntity member = memberRepository.findById(accrual.getMemberId())
					.orElseThrow(() -> new MemberNotFoundException("Member not found: " + accrual.getMemberId()));
			
			log.info("Points already accrued for booking. bookingId={}, pointsCredited={}", 
					bookingId, accrual.getPointsCredited());
			
			return new AccrualResult(
					member.getMemberId(),
					bookingId,
					accrual.getPointsCredited(),
					member.getPointsBalance(),
					member.getTier(),
					member.getStatus(),
					true // already accrued
			);
		}

		// Get member
		LoyaltyMemberEntity member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));

		// Calculate points: 1 point per dollar spent
		int pointsToCredit = amount.intValue();

		// Update member points balance
		member.setPointsBalance(member.getPointsBalance() + pointsToCredit);

		// Update tier based on new balance
		updateMemberTier(member);

		memberRepository.save(member);

		// Record accrual for idempotency
		try {
			LoyaltyAccrualEntity accrual = new LoyaltyAccrualEntity();
			accrual.setBookingId(bookingId);
			accrual.setMemberId(memberId);
			accrual.setPointsCredited(pointsToCredit);
			accrual.setAmount(amount);
			accrual.setCurrency(currency);
			accrual.setCorrelationId(correlationId);
			accrual.setCreatedAt(Instant.now());
			accrualRepository.save(accrual);

			log.info("Points accrued successfully. memberId={}, bookingId={}, pointsCredited={}, newBalance={}, tier={}",
					memberId, bookingId, pointsToCredit, member.getPointsBalance(), member.getTier());

			return new AccrualResult(
					member.getMemberId(),
					bookingId,
					pointsToCredit,
					member.getPointsBalance(),
					member.getTier(),
					member.getStatus(),
					false // newly accrued
			);
		} catch (DataIntegrityViolationException e) {
			// Race condition: another thread already accrued points for this booking
			log.warn("Duplicate accrual detected via constraint violation. bookingId={}", bookingId);
			// Re-fetch the accrual and member to return consistent data
			LoyaltyAccrualEntity accrual = accrualRepository.findById(bookingId)
					.orElseThrow(() -> new IllegalStateException("Accrual should exist after constraint violation"));
			LoyaltyMemberEntity updatedMember = memberRepository.findById(accrual.getMemberId())
					.orElseThrow(() -> new MemberNotFoundException("Member not found: " + accrual.getMemberId()));
			
			return new AccrualResult(
					updatedMember.getMemberId(),
					bookingId,
					accrual.getPointsCredited(),
					updatedMember.getPointsBalance(),
					updatedMember.getTier(),
					updatedMember.getStatus(),
					true // already accrued
			);
		}
	}

	private void updateMemberTier(LoyaltyMemberEntity member) {
		int balance = member.getPointsBalance();
		String newTier;
		
		if (balance >= 50000) {
			newTier = "PLATINUM";
		} else if (balance >= 15000) {
			newTier = "GOLD";
		} else if (balance >= 5000) {
			newTier = "SILVER";
		} else {
			newTier = "BASIC";
		}

		if (!newTier.equals(member.getTier())) {
			log.info("Member tier upgraded. memberId={}, oldTier={}, newTier={}, balance={}",
					member.getMemberId(), member.getTier(), newTier, balance);
			member.setTier(newTier);
		}
	}

	public static class AccrualResult {
		public final UUID memberId;
		public final UUID bookingId;
		public final int pointsCredited;
		public final int newPointsBalance;
		public final String tier;
		public final String status;
		public final boolean alreadyAccrued;

		public AccrualResult(UUID memberId, UUID bookingId, int pointsCredited, int newPointsBalance,
		                     String tier, String status, boolean alreadyAccrued) {
			this.memberId = memberId;
			this.bookingId = bookingId;
			this.pointsCredited = pointsCredited;
			this.newPointsBalance = newPointsBalance;
			this.tier = tier;
			this.status = status;
			this.alreadyAccrued = alreadyAccrued;
		}
	}

	public static class MemberNotFoundException extends RuntimeException {
		public MemberNotFoundException(String message) {
			super(message);
		}
	}
}
