package framework.endpoints;

/**
 * Endpoint constants for Inventory Service.
 */
public final class InventoryEndpoints {

    public static final String API_DOCS = "/v3/api-docs";
    public static final String RESERVATIONS = "/inventory/reservations/{reservationId}";
    public static final String BY_BOOKING = "/inventory/reservations/by-booking/{bookingId}";
    public static final String ADMIN_SEED = "/inventory/admin/seed";
    public static final String ADMIN_RESET = "/inventory/admin/reset";

    private InventoryEndpoints() {
        // Constants class
    }

    public static String reservations(String reservationId) {
        return RESERVATIONS.replace("{reservationId}", reservationId);
    }

    public static String byBooking(String bookingId) {
        return BY_BOOKING.replace("{bookingId}", bookingId);
    }
}
