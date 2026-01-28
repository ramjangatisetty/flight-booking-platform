package com.letzautomate.baggage.api.controller;

import com.letzautomate.baggage.api.dto.SeedResponse;
import com.letzautomate.baggage.application.BaggageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/baggage/admin")
public class BaggageAdminController {

	private final BaggageService baggageService;

	public BaggageAdminController(BaggageService baggageService) {
		this.baggageService = baggageService;
	}

	@PostMapping(value = "/seed", produces = MediaType.APPLICATION_XML_VALUE)
	public SeedResponse seed() {
		String bagTag = baggageService.seedDemoData();
		return new SeedResponse(bagTag, "Demo baggage data seeded successfully with 3 events");
	}
}
