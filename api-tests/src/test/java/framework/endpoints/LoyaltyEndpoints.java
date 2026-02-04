package framework.endpoints;

/**
 * Endpoint constants for Loyalty Service (SOAP + REST admin).
 */
public final class LoyaltyEndpoints {

    // SOAP endpoints
    public static final String SOAP_ENDPOINT = "/ws";
    public static final String WSDL = "/ws/loyalty.wsdl";

    // REST admin endpoints
    public static final String ADMIN_SEED = "/loyalty/admin/seed";
    public static final String ADMIN_RESET = "/loyalty/admin/reset";

    // SOAP Actions
    public static final String SOAP_ACTION_ENROLL = "http://letzautomate.com/loyalty/v1/EnrollMember";
    public static final String SOAP_ACTION_STATUS = "http://letzautomate.com/loyalty/v1/GetMemberStatus";
    public static final String SOAP_ACTION_ACCRUE = "http://letzautomate.com/loyalty/v1/AccruePoints";

    private LoyaltyEndpoints() {
        // Constants class
    }
}
