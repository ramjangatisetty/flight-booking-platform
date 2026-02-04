package framework.bdd;

import framework.clients.ApiClient;
import framework.soap.SoapClient;
import framework.soap.SoapResponse;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared context for Cucumber step definitions.
 * Stores state between steps within a scenario.
 */
public class TestContext {

    private ApiClient client;
    private SoapClient soapClient;
    private final Map<String, String> headers = new HashMap<>();
    private Response lastResponse;
    private SoapResponse lastSoapResponse;
    private Object lastRequestBody;
    private final Map<String, Object> storage = new HashMap<>();

    public void setClient(ApiClient client) {
        this.client = client;
    }

    public ApiClient getClient() {
        return client;
    }

    public void setSoapClient(SoapClient soapClient) {
        this.soapClient = soapClient;
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

    public void reset() {
        client = null;
        soapClient = null;
        headers.clear();
        lastResponse = null;
        lastSoapResponse = null;
        lastRequestBody = null;
        storage.clear();
    }
}
