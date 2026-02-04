package framework.soap;

/**
 * Model representing a SOAP fault response.
 */
public class SoapFault {

    private String faultCode;
    private String faultString;
    private String faultDetail;
    private String loyaltyFaultCode;
    private String loyaltyFaultMessage;

    public SoapFault() {
    }

    public SoapFault(String faultCode, String faultString, String faultDetail) {
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultDetail = faultDetail;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    public String getFaultDetail() {
        return faultDetail;
    }

    public void setFaultDetail(String faultDetail) {
        this.faultDetail = faultDetail;
    }

    public String getLoyaltyFaultCode() {
        return loyaltyFaultCode;
    }

    public void setLoyaltyFaultCode(String loyaltyFaultCode) {
        this.loyaltyFaultCode = loyaltyFaultCode;
    }

    public String getLoyaltyFaultMessage() {
        return loyaltyFaultMessage;
    }

    public void setLoyaltyFaultMessage(String loyaltyFaultMessage) {
        this.loyaltyFaultMessage = loyaltyFaultMessage;
    }

    @Override
    public String toString() {
        return "SoapFault{" +
                "faultCode='" + faultCode + '\'' +
                ", faultString='" + faultString + '\'' +
                ", loyaltyFaultCode='" + loyaltyFaultCode + '\'' +
                ", loyaltyFaultMessage='" + loyaltyFaultMessage + '\'' +
                '}';
    }
}
