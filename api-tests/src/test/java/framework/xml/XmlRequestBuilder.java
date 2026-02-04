package framework.xml;

public abstract class XmlRequestBuilder {
    protected String namespace = "http://letzautomate.com/baggage/v1";

    public XmlRequestBuilder withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public abstract String build();
}
