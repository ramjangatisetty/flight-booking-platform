package framework.models.response;

import java.math.BigDecimal;

/**
 * Response model for booking operations.
 * Matches BookingResponse from booking-service.
 */
public class BookingResponse {

    private String bookingId;
    private String correlationId;
    private String flightId;
    private String seatClass;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String memberId;
    private String loyaltyAccrualStatus;
    private Integer loyaltyPoints;
    private String bagTag;

    public BookingResponse() {
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getBagTag() {
        return bagTag;
    }

    public void setBagTag(String bagTag) {
        this.bagTag = bagTag;
    }
}
