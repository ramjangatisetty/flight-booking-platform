package com.letzautomate.inventory.api;

import com.letzautomate.inventory.application.InventoryService;
import com.letzautomate.inventory.infrastructure.persistence.InventoryItemEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/admin")
public class InventoryAdminController {

	private final InventoryService inventoryService;

	public InventoryAdminController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping("/seed")
	public InventoryItemEntity seed(@Valid @RequestBody SeedRequest req) {
		return inventoryService.seedInventory(req.flightId(), req.seatClass(), req.availableSeats());
	}

	@PostMapping("/reset")
	public void reset() {
		inventoryService.resetDemoState();
	}

	public record SeedRequest(
			@NotBlank String flightId,
			@NotBlank String seatClass,
			@Min(0) int availableSeats
	) {}
}