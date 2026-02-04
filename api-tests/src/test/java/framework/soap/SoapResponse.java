package framework.soap;

/**
 * Model representing a SOAP response.
 */
public class SoapResponse {

    private final int statusCode;
    private final String rawResponse;
    private final SoapFault fault;

    public SoapResponse(int statusCode, String rawResponse) {
        this.statusCode = statusCode;
        this.rawResponse = rawResponse;
        this.fault = SoapResponseParser.extractFault(rawResponse);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public boolean isFault() {
        return fault != null;
    }

    public String getBody() {
        return SoapResponseParser.extractBody(rawResponse);
    }

    public SoapFault getFault() {
        return fault;
    }

    @Override
    public String toString() {
        return "SoapResponse{" +
                "statusCode=" + statusCode +
                ", isFault=" + isFault() +
                '}';
    }
}
