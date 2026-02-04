package framework.requests;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class BookingRequests {
    private BookingRequests() {}

    public static Map<String, Object> validCreateBooking() {
        return createBooking("FL" + System.currentTimeMillis(), "ECONOMY", "299.99", "USD", null);
    }

    public static Map<String, Object> createBooking(String flightId, String seatClass) {
        return createBooking(flightId, seatClass, "299.99", "USD", null);
    }

    public static Map<String, Object> createBooking(String flightId, String seatClass,
                                                     String price, String currency, String memberId) {
        Map<String, Object> request = new HashMap<>();
        request.put("flightId", flightId);
        request.put("seatClass", seatClass);
        request.put("price", new BigDecimal(price));
        request.put("currency", currency);
        if (memberId != null) {
            request.put("memberId", memberId);
        }
        return request;
    }
}
