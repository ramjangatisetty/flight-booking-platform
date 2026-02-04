package framework.endpoints;

public final class LoyaltyEndpoints {
    public static final String SOAP_ENDPOINT = "/ws";
    public static final String WSDL = "/ws/loyalty.wsdl";
    public static final String ADMIN_SEED = "/loyalty/admin/seed";
    public static final String ADMIN_RESET = "/loyalty/admin/reset";
    public static final String SOAP_ACTION_ENROLL = "http://letzautomate.com/loyalty/v1/EnrollMember";
    public static final String SOAP_ACTION_STATUS = "http://letzautomate.com/loyalty/v1/GetMemberStatus";
    public static final String SOAP_ACTION_ACCRUE = "http://letzautomate.com/loyalty/v1/AccruePoints";

    private LoyaltyEndpoints() {}

    public static String getSoapAction(String operation) {
        return switch (operation) {
            case "EnrollMember" -> SOAP_ACTION_ENROLL;
            case "GetMemberStatus" -> SOAP_ACTION_STATUS;
            case "AccruePoints" -> SOAP_ACTION_ACCRUE;
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }
}
