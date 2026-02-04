package framework.endpoints;

public final class BaggageEndpoints {
    public static final String HEALTH = "/actuator/health";
    public static final String CHECKIN = "/baggage/checkin";
    public static final String STATUS = "/baggage/status/{bagTag}";
    public static final String TRACK = "/baggage/track/{bagTag}";
    public static final String ADMIN_SEED = "/baggage/admin/seed";

    private BaggageEndpoints() {}
}
