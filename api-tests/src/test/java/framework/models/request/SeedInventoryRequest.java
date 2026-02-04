package framework.models.request;

/**
 * Request model for seeding inventory.
 */
public record SeedInventoryRequest(
        String flightId,
        String seatClass,
        int availableSeats
) {
}
