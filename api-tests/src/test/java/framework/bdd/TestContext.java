package framework.bdd;

import framework.clients.ApiClient;
import framework.clients.RestAssuredApiClient;
import framework.config.ServiceType;
import framework.config.TestConfig;
import framework.soap.SoapClient;
import framework.soap.SoapClientImpl;
import framework.soap.SoapResponse;
import framework.xml.XmlApiClient;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private ApiClient client;
    private SoapClient soapClient;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, Object> storage = new HashMap<>();
    private Response lastResponse;
    private SoapResponse lastSoapResponse;
    private Object lastRequestBody;

    public void setClient(ServiceType serviceType) {
        String baseUrl = TestConfig.getInstance().getBaseUrl(serviceType);
        // Always use RestAssuredApiClient - XmlApiClient is used only for XML-specific requests
        this.client = new RestAssuredApiClient(baseUrl);
    }

    public void setXmlClient(ServiceType serviceType) {
        String baseUrl = TestConfig.getInstance().getBaseUrl(serviceType);
        this.client = new XmlApiClient(baseUrl);
    }

    public void setSoapClient(ServiceType serviceType) {
        String baseUrl = TestConfig.getInstance().getBaseUrl(serviceType);
        this.soapClient = new SoapClientImpl(baseUrl);
    }

    public ApiClient getClient() {
        return client;
    }

    public SoapClient getSoapClient() {
        return soapClient;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public void clearHeaders() {
        headers.clear();
    }

    public void set(String key, Object value) {
        storage.put(key, value);
    }

    public Object get(String key) {
        return storage.get(key);
    }

    public String getString(String key) {
        Object value = storage.get(key);
        return value != null ? value.toString() : null;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public SoapResponse getLastSoapResponse() {
        return lastSoapResponse;
    }

    public void setLastSoapResponse(SoapResponse lastSoapResponse) {
        this.lastSoapResponse = lastSoapResponse;
    }

    public Object getLastRequestBody() {
        return lastRequestBody;
    }

    public void setLastRequestBody(Object lastRequestBody) {
        this.lastRequestBody = lastRequestBody;
    }

    public void reset() {
        headers.clear();
        storage.clear();
        lastResponse = null;
        lastSoapResponse = null;
        lastRequestBody = null;
    }
}
