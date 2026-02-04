package framework.models.request;

import java.math.BigDecimal;

/**
 * Request model for creating a booking.
 * Matches CreateBookingRequest from booking-service.
 */
public record CreateBookingRequest(
        String flightId,
        String seatClass,
        BigDecimal amount,
        String currency,
        String memberId
) {
    /**
     * Constructor without optional memberId.
     */
    public CreateBookingRequest(String flightId, String seatClass, BigDecimal amount, String currency) {
        this(flightId, seatClass, amount, currency, null);
    }
}
