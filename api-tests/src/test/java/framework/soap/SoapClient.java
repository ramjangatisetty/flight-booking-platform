package framework.soap;

/**
 * Interface for SOAP client operations.
 */
public interface SoapClient {

    SoapResponse sendRequest(String soapAction, String envelope);
}
