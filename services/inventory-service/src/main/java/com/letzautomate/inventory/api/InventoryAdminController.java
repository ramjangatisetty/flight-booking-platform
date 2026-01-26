package com.letzautomate.inventory.api;

import com.letzautomate.inventory.application.InventoryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/admin")
@Profile("local")
public class InventoryAdminController {

	private final InventoryService inventoryService;

	public InventoryAdminController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping("/seed")
	@ResponseStatus(HttpStatus.CREATED)
	public SeedResponse seed(@RequestBody SeedRequest req) {
		var item = inventoryService.seedInventory(req.flightId(), req.seatClass(), req.availableSeats());
		return new SeedResponse(item.getFlightId(), item.getSeatClass(), item.getAvailableSeats());
	}

	@PostMapping("/reset")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reset() {
		inventoryService.resetDemoState();
	}

	public record SeedRequest(
			@NotBlank String flightId,
			@NotBlank String seatClass,
			@Min(0) int availableSeats
	) {}

	public record SeedResponse(String flightId, String seatClass, int availableSeats) {}
}