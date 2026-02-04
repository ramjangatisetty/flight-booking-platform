package framework.xml;

public class BaggageCheckinXmlBuilder extends XmlRequestBuilder {
    private String bookingId;
    private String passengerId;
    private String bagTag;
    private String origin;
    private String destination;
    private Double weight;

    public BaggageCheckinXmlBuilder withBookingId(String bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public BaggageCheckinXmlBuilder withPassengerId(String passengerId) {
        this.passengerId = passengerId;
        return this;
    }

    public BaggageCheckinXmlBuilder withBagTag(String bagTag) {
        this.bagTag = bagTag;
        return this;
    }

    public BaggageCheckinXmlBuilder withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public BaggageCheckinXmlBuilder withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public BaggageCheckinXmlBuilder withWeight(Double weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<BaggageCheckinRequest xmlns=\"").append(namespace).append("\">\n");
        if (bookingId != null) sb.append("  <bookingId>").append(bookingId).append("</bookingId>\n");
        if (passengerId != null) sb.append("  <passengerId>").append(passengerId).append("</passengerId>\n");
        if (bagTag != null) sb.append("  <bagTag>").append(bagTag).append("</bagTag>\n");
        if (origin != null) sb.append("  <origin>").append(origin).append("</origin>\n");
        if (destination != null) sb.append("  <destination>").append(destination).append("</destination>\n");
        if (weight != null) sb.append("  <weight>").append(weight).append("</weight>\n");
        sb.append("</BaggageCheckinRequest>");
        return sb.toString();
    }
}
