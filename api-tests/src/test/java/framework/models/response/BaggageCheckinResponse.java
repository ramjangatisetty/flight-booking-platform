package framework.models.response;

/**
 * Response model for baggage check-in operations.
 */
public class BaggageCheckinResponse {

    private String bagTag;
    private String bookingId;
    private String passengerId;
    private String status;
    private String origin;
    private String destination;

    public BaggageCheckinResponse() {
    }

    public String getBagTag() {
        return bagTag;
    }

    public void setBagTag(String bagTag) {
        this.bagTag = bagTag;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
