package framework.soap;

public class SoapResponse {
    private final int statusCode;
    private final String rawResponse;
    private final SoapFault fault;

    public SoapResponse(int statusCode, String rawResponse, SoapFault fault) {
        this.statusCode = statusCode;
        this.rawResponse = rawResponse;
        this.fault = fault;
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

    public SoapFault getFault() {
        return fault;
    }

    public String getBody() {
        return SoapResponseParser.extractBody(rawResponse);
    }
}
