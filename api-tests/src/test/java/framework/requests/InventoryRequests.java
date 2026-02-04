package framework.requests;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating Inventory Service request payloads.
 */
public final class InventoryRequests {

    private InventoryRequests() {
        // Utility class
    }

    public static Map<String, Object> seedInventory(String flightId, String seatClass, int availableSeats) {
        Map<String, Object> request = new HashMap<>();
        request.put("flightId", flightId);
        request.put("seatClass", seatClass);
        request.put("availableSeats", availableSeats);
        return request;
    }
}
