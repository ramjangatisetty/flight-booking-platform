package framework.xml;

/**
 * Builder for Baggage Status Update XML request payloads.
 */
public class BaggageStatusUpdateXmlBuilder extends XmlRequestBuilder<BaggageStatusUpdateXmlBuilder> {

    public static final String DEFAULT_NAMESPACE = "http://letzautomate.com/baggage/v1";

    private String status;
    private String location;

    public BaggageStatusUpdateXmlBuilder() {
        this.namespace = DEFAULT_NAMESPACE;
    }

    public BaggageStatusUpdateXmlBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public BaggageStatusUpdateXmlBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("<BaggageStatusUpdateRequest").append(getNamespaceAttribute()).append(">");
        if (status != null) {
            sb.append("<status>").append(status).append("</status>");
        }
        if (location != null) {
            sb.append("<location>").append(location).append("</location>");
        }
        sb.append("</BaggageStatusUpdateRequest>");
        return sb.toString();
    }
}
