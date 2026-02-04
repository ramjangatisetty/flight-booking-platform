package framework.soap;

public class LoyaltySoapRequestBuilder {
    private static final String NAMESPACE = "http://letzautomate.com/loyalty/v1";

    public static String enrollMember(String firstName, String lastName, String email) {
        String body = """
            <loy:EnrollMemberRequest>
                <loy:firstName>%s</loy:firstName>
                <loy:lastName>%s</loy:lastName>
                <loy:email>%s</loy:email>
            </loy:EnrollMemberRequest>
            """.formatted(firstName, lastName, email);
        return new SoapEnvelopeBuilder().withNamespace(NAMESPACE).withBody(body).build();
    }

    public static String getMemberStatus(String memberId) {
        String body = """
            <loy:GetMemberStatusRequest>
                <loy:memberId>%s</loy:memberId>
            </loy:GetMemberStatusRequest>
            """.formatted(memberId);
        return new SoapEnvelopeBuilder().withNamespace(NAMESPACE).withBody(body).build();
    }

    public static String accruePoints(String memberId, String bookingId, String amount,
                                       String currency, String correlationId) {
        String body = """
            <loy:AccruePointsRequest>
                <loy:memberId>%s</loy:memberId>
                <loy:bookingId>%s</loy:bookingId>
                <loy:amount>%s</loy:amount>
                <loy:currency>%s</loy:currency>
                <loy:correlationId>%s</loy:correlationId>
            </loy:AccruePointsRequest>
            """.formatted(memberId, bookingId, amount, currency, correlationId);
        return new SoapEnvelopeBuilder().withNamespace(NAMESPACE).withBody(body).build();
    }
}
