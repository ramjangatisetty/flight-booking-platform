package framework.endpoints;

/**
 * Endpoint constants for Baggage Service (XML REST).
 */
public final class BaggageEndpoints {

    public static final String HEALTH = "/actuator/health";
    public static final String CHECKIN = "/baggage/checkin";
    public static final String STATUS = "/baggage/status/{bagTag}";
    public static final String TRACK = "/baggage/track/{bagTag}";
    public static final String TRACK_BY_BOOKING = "/baggage/booking/{bookingId}";
    public static final String ADMIN_SEED = "/baggage/admin/seed";

    private BaggageEndpoints() {
        // Constants class
    }

    public static String status(String bagTag) {
        return STATUS.replace("{bagTag}", bagTag);
    }

    public static String track(String bagTag) {
        return TRACK.replace("{bagTag}", bagTag);
    }

    public static String trackByBooking(String bookingId) {
        return TRACK_BY_BOOKING.replace("{bookingId}", bookingId);
    }
}
