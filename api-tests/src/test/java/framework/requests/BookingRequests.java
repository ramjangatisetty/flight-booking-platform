package framework.requests;

import framework.utils.UuidUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating Booking Service request payloads.
 * Matches CreateBookingRequest from booking-service.
 */
public final class BookingRequests {

    private BookingRequests() {
        // Utility class
    }

    /**
     * Creates a valid booking request with default values.
     */
    public static Map<String, Object> validCreateBooking() {
        return createBooking("FL123", "ECONOMY", new BigDecimal("299.99"), "USD", null);
    }

    /**
     * Creates a valid booking request with a loyalty member ID.
     */
    public static Map<String, Object> validCreateBookingWithMember(String memberId) {
        return createBooking("FL123", "ECONOMY", new BigDecimal("299.99"), "USD", memberId);
    }

    /**
     * Creates a booking request with custom values.
     */
    public static Map<String, Object> createBooking(String flightId, String seatClass, 
                                                     BigDecimal amount, String currency, String memberId) {
        Map<String, Object> request = new HashMap<>();
        request.put("flightId", flightId);
        request.put("seatClass", seatClass);
        request.put("amount", amount);
        request.put("currency", currency);
        if (memberId != null) {
            request.put("memberId", memberId);
        }
        return request;
    }

    /**
     * Creates a booking request with specified flight ID.
     */
    public static Map<String, Object> withFlightId(String flightId) {
        return createBooking(flightId, "ECONOMY", new BigDecimal("299.99"), "USD", null);
    }

    /**
     * Creates a booking request for a specific flight and seat class.
     * Useful for E2E tests where inventory is seeded for specific flight/class combinations.
     */
    public static Map<String, Object> createBookingForFlight(String flightId, String seatClass) {
        return createBooking(flightId, seatClass, new BigDecimal("299.99"), "USD", null);
    }

    /**
     * Creates a booking request with specified seat class.
     */
    public static Map<String, Object> withSeatClass(String seatClass) {
        return createBooking("FL123", seatClass, new BigDecimal("299.99"), "USD", null);
    }

    /**
     * Creates a booking request with specified amount.
     */
    public static Map<String, Object> withAmount(BigDecimal amount) {
        return createBooking("FL123", "ECONOMY", amount, "USD", null);
    }

    /**
     * Creates an empty request (for negative testing).
     */
    public static Map<String, Object> emptyRequest() {
        return new HashMap<>();
    }

    /**
     * Creates a request missing required fields (for negative testing).
     */
    public static Map<String, Object> missingFlightId() {
        Map<String, Object> request = new HashMap<>();
        request.put("seatClass", "ECONOMY");
        request.put("amount", new BigDecimal("299.99"));
        request.put("currency", "USD");
        return request;
    }

    /**
     * Creates a request with invalid currency format (for negative testing).
     */
    public static Map<String, Object> invalidCurrency() {
        return createBooking("FL123", "ECONOMY", new BigDecimal("299.99"), "US", null);
    }

    /**
     * Creates a request with invalid seat class (for negative testing).
     */
    public static Map<String, Object> invalidSeatClass() {
        Map<String, Object> request = new HashMap<>();
        request.put("flightId", "FL123");
        request.put("seatClass", "INVALID_CLASS");
        request.put("amount", new BigDecimal("299.99"));
        request.put("currency", "USD");
        return request;
    }

    /**
     * Creates a request with zero amount (for negative testing).
     */
    public static Map<String, Object> zeroAmount() {
        return createBooking("FL123", "ECONOMY", BigDecimal.ZERO, "USD", null);
    }
}
