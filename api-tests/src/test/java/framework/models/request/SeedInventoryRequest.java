package framework.models.request;

public record SeedInventoryRequest(
        String flightId,
        String seatClass,
        int availableSeats
) {}
