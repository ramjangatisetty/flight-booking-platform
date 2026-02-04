package framework.models.request;

import java.math.BigDecimal;

public record CreateBookingRequest(
        String flightId,
        String seatClass,
        BigDecimal amount,
        String currency,
        String memberId
) {}
