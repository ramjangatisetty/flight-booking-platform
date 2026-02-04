package framework.soap;

public class SoapEnvelopeBuilder {
    private String namespace = "http://letzautomate.com/loyalty/v1";
    private String bodyContent = "";

    public SoapEnvelopeBuilder withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public SoapEnvelopeBuilder withBody(String body) {
        this.bodyContent = body;
        return this;
    }

    public String build() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                           xmlns:loy="%s">
                <soap:Body>
                    %s
                </soap:Body>
            </soap:Envelope>
            """.formatted(namespace, bodyContent);
    }
}
