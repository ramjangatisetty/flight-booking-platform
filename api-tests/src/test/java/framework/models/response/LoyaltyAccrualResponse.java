package framework.models.response;

/**
 * Response model for loyalty accrual queries.
 * Matches LoyaltyAccrualResponse from booking-service.
 */
public class LoyaltyAccrualResponse {

    private String bookingId;
    private String memberId;
    private String loyaltyAccrualStatus;
    private Integer loyaltyPoints;
    private String loyaltyAccruedAt;

    public LoyaltyAccrualResponse() {
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getLoyaltyAccrualStatus() {
        return loyaltyAccrualStatus;
    }

    public void setLoyaltyAccrualStatus(String loyaltyAccrualStatus) {
        this.loyaltyAccrualStatus = loyaltyAccrualStatus;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getLoyaltyAccruedAt() {
        return loyaltyAccruedAt;
    }

    public void setLoyaltyAccruedAt(String loyaltyAccruedAt) {
        this.loyaltyAccruedAt = loyaltyAccruedAt;
    }
}
