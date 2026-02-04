package framework.models.response;

import java.util.List;

/**
 * Response model for baggage tracking queries.
 */
public class BaggageTrackResponse {

    private String bagTag;
    private String currentStatus;
    private String currentLocation;
    private List<BaggageEvent> events;

    public BaggageTrackResponse() {
    }

    public String getBagTag() {
        return bagTag;
    }

    public void setBagTag(String bagTag) {
        this.bagTag = bagTag;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<BaggageEvent> getEvents() {
        return events;
    }

    public void setEvents(List<BaggageEvent> events) {
        this.events = events;
    }

    public static class BaggageEvent {
        private String status;
        private String location;
        private String timestamp;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
