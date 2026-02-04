package framework.soap;

public class SoapFault {
    private final String faultCode;
    private final String faultString;
    private final String faultDetail;
    private final String loyaltyFaultCode;
    private final String loyaltyFaultMessage;

    public SoapFault(String faultCode, String faultString, String faultDetail,
                     String loyaltyFaultCode, String loyaltyFaultMessage) {
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultDetail = faultDetail;
        this.loyaltyFaultCode = loyaltyFaultCode;
        this.loyaltyFaultMessage = loyaltyFaultMessage;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public String getFaultDetail() {
        return faultDetail;
    }

    public String getLoyaltyFaultCode() {
        return loyaltyFaultCode;
    }

    public String getLoyaltyFaultMessage() {
        return loyaltyFaultMessage;
    }
}
