package framework.soap;

public interface SoapClient {
    SoapResponse sendRequest(String soapAction, String envelope);
}
