package framework.endpoints;

/**
 * Endpoint constants for Booking Service.
 */
public final class BookingEndpoints {

    public static final String API_DOCS = "/v3/api-docs";
    public static final String BASE = "/bookings";
    public static final String BY_ID = "/bookings/{id}";
    public static final String STATUS = "/bookings/{id}/status";
    public static final String LOYALTY = "/bookings/{id}/loyalty";

    private BookingEndpoints() {
        // Constants class
    }

    public static String byId(String id) {
        return BY_ID.replace("{id}", id);
    }

    public static String status(String id) {
        return STATUS.replace("{id}", id);
    }

    public static String loyalty(String id) {
        return LOYALTY.replace("{id}", id);
    }
}
