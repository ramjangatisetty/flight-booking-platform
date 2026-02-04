package framework.endpoints;

public final class BookingEndpoints {
    public static final String API_DOCS = "/v3/api-docs";
    public static final String BASE = "/bookings";
    public static final String BY_ID = "/bookings/{id}";
    public static final String STATUS = "/bookings/{id}/status";
    public static final String LOYALTY = "/bookings/{id}/loyalty";

    private BookingEndpoints() {}
}
