package framework.soap;

/**
 * Builder for constructing SOAP envelope requests.
 */
public class SoapEnvelopeBuilder {

    private static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    private String namespace;
    private String bodyContent;

    public SoapEnvelopeBuilder withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public SoapEnvelopeBuilder withBody(String bodyContent) {
        this.bodyContent = bodyContent;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"").append(SOAP_ENV_NS).append("\"");
        if (namespace != null && !namespace.isBlank()) {
            sb.append(" xmlns:ns=\"").append(namespace).append("\"");
        }
        sb.append(">");
        sb.append("<soap:Header/>");
        sb.append("<soap:Body>");
        if (bodyContent != null) {
            sb.append(bodyContent);
        }
        sb.append("</soap:Body>");
        sb.append("</soap:Envelope>");
        return sb.toString();
    }
}
