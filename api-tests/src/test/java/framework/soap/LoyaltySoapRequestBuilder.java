package framework.soap;

/**
 * Builder for Loyalty Service SOAP request payloads.
 */
public final class LoyaltySoapRequestBuilder {

    public static final String NAMESPACE = "http://letzautomate.com/loyalty/v1";

    private LoyaltySoapRequestBuilder() {
        // Utility class
    }

    public static String enrollMember(String firstName, String lastName, String email) {
        String body = String.format(
                "<ns:EnrollMemberRequest xmlns:ns=\"%s\">" +
                        "<ns:firstName>%s</ns:firstName>" +
                        "<ns:lastName>%s</ns:lastName>" +
                        "<ns:email>%s</ns:email>" +
                        "</ns:EnrollMemberRequest>",
                NAMESPACE, firstName, lastName, email
        );
        return new SoapEnvelopeBuilder()
                .withNamespace(NAMESPACE)
                .withBody(body)
                .build();
    }

    public static String getMemberStatus(String memberId) {
        String body = String.format(
                "<ns:GetMemberStatusRequest xmlns:ns=\"%s\">" +
                        "<ns:memberId>%s</ns:memberId>" +
                        "</ns:GetMemberStatusRequest>",
                NAMESPACE, memberId
        );
        return new SoapEnvelopeBuilder()
                .withNamespace(NAMESPACE)
                .withBody(body)
                .build();
    }

    public static String accruePoints(String memberId, String bookingId, String amount, String currency, String correlationId) {
        StringBuilder body = new StringBuilder();
        body.append(String.format("<ns:AccruePointsRequest xmlns:ns=\"%s\">", NAMESPACE));
        body.append(String.format("<ns:memberId>%s</ns:memberId>", memberId));
        body.append(String.format("<ns:bookingId>%s</ns:bookingId>", bookingId));
        body.append(String.format("<ns:amount>%s</ns:amount>", amount));
        body.append(String.format("<ns:currency>%s</ns:currency>", currency));
        if (correlationId != null && !correlationId.isBlank()) {
            body.append(String.format("<ns:correlationId>%s</ns:correlationId>", correlationId));
        }
        body.append("</ns:AccruePointsRequest>");

        return new SoapEnvelopeBuilder()
                .withNamespace(NAMESPACE)
                .withBody(body.toString())
                .build();
    }
}
