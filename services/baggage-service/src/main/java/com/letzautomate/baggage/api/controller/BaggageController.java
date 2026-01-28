package com.letzautomate.baggage.api.controller;

import com.letzautomate.baggage.api.dto.BaggageCheckinRequest;
import com.letzautomate.baggage.api.dto.BaggageCheckinResponse;
import com.letzautomate.baggage.api.dto.BaggageStatusUpdateRequest;
import com.letzautomate.baggage.api.dto.BaggageStatusUpdateResponse;
import com.letzautomate.baggage.api.dto.BaggageTrackResponse;
import com.letzautomate.baggage.application.BaggageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/baggage")
public class BaggageController {

	private final BaggageService baggageService;

	public BaggageController(BaggageService baggageService) {
		this.baggageService = baggageService;
	}

	@PostMapping(value = "/checkin", 
	             consumes = MediaType.APPLICATION_XML_VALUE, 
	             produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public BaggageCheckinResponse checkin(@Valid @RequestBody BaggageCheckinRequest request) {
		return baggageService.checkinBaggage(request);
	}

	@PutMapping(value = "/status/{bagTag}",
	            consumes = MediaType.APPLICATION_XML_VALUE,
	            produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public BaggageStatusUpdateResponse updateStatus(
			@PathVariable String bagTag,
			@Valid @RequestBody BaggageStatusUpdateRequest request) {
		return baggageService.updateBaggageStatus(bagTag, request);
	}

	@GetMapping(value = "/track/{bagTag}", 
	            produces = MediaType.APPLICATION_XML_VALUE)
	public BaggageTrackResponse track(@PathVariable String bagTag) {
		return baggageService.trackBaggage(bagTag);
	}
}
