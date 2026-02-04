package framework.xml;

/**
 * Builder for Baggage Check-in XML request payloads.
 */
public class BaggageCheckinXmlBuilder extends XmlRequestBuilder<BaggageCheckinXmlBuilder> {

    public static final String DEFAULT_NAMESPACE = "http://letzautomate.com/baggage/v1";

    private String bookingId;
    private String passengerId;
    private String bagTag;
    private String origin;
    private String destination;

    public BaggageCheckinXmlBuilder() {
        this.namespace = DEFAULT_NAMESPACE;
    }

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

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("<BaggageCheckinRequest").append(getNamespaceAttribute()).append(">");
        if (bookingId != null) {
            sb.append("<bookingId>").append(bookingId).append("</bookingId>");
        }
        if (passengerId != null) {
            sb.append("<passengerId>").append(passengerId).append("</passengerId>");
        }
        if (bagTag != null) {
            sb.append("<bagTag>").append(bagTag).append("</bagTag>");
        }
        if (origin != null) {
            sb.append("<origin>").append(origin).append("</origin>");
        }
        if (destination != null) {
            sb.append("<destination>").append(destination).append("</destination>");
        }
        sb.append("</BaggageCheckinRequest>");
        return sb.toString();
    }
}
