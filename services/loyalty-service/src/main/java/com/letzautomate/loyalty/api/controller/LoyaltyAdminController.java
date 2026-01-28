package com.letzautomate.loyalty.api.controller;

import com.letzautomate.loyalty.application.LoyaltyService;
import com.letzautomate.loyalty.infrastructure.persistence.entity.LoyaltyMemberEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/loyalty/admin")
public class LoyaltyAdminController {

	private static final Logger log = LoggerFactory.getLogger(LoyaltyAdminController.class);

	private final LoyaltyService loyaltyService;

	public LoyaltyAdminController(LoyaltyService loyaltyService) {
		this.loyaltyService = loyaltyService;
	}

	@PostMapping("/seed")
	public List<MemberResponse> seed() {
		log.info("Seeding loyalty members");

		List<MemberResponse> members = new ArrayList<>();

		try {
			// Seed SILVER tier member
			LoyaltyMemberEntity silver = loyaltyService.seedMember(
					"John", "Silver", "john.silver@example.com", "SILVER", 5000
			);
			members.add(toResponse(silver));

			// Seed GOLD tier member
			LoyaltyMemberEntity gold = loyaltyService.seedMember(
					"Jane", "Gold", "jane.gold@example.com", "GOLD", 15000
			);
			members.add(toResponse(gold));

			// Seed PLATINUM tier member
			LoyaltyMemberEntity platinum = loyaltyService.seedMember(
					"Bob", "Platinum", "bob.platinum@example.com", "PLATINUM", 50000
			);
			members.add(toResponse(platinum));

			log.info("Seeded {} loyalty members", members.size());
		} catch (IllegalArgumentException e) {
			log.warn("Some members already exist: {}", e.getMessage());
		}

		return members;
	}

	@PostMapping("/reset")
	public void reset() {
		log.info("Resetting loyalty members");
		loyaltyService.resetDemoState();
	}

	private MemberResponse toResponse(LoyaltyMemberEntity entity) {
		return new MemberResponse(
				entity.getMemberId().toString(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getEmail(),
				entity.getTier(),
				entity.getStatus(),
				entity.getPointsBalance()
		);
	}

	public record MemberResponse(
			String memberId,
			String firstName,
			String lastName,
			String email,
			String tier,
			String status,
			int pointsBalance
	) {}
}
