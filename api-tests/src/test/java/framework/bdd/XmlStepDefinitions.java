package framework.bdd;

import framework.reporting.ReportLogger;
import framework.xml.BaggageCheckinXmlBuilder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlStepDefinitions {
    private final TestContext context;
    private String xmlNamespace = "http://letzautomate.com/baggage/v1";
    private BaggageCheckinXmlBuilder xmlBuilder;

    public XmlStepDefinitions(TestContext context) {
        this.context = context;
    }

    @Given("I have a valid BaggageCheckinRequest XML payload")
    public void iHaveAValidBaggageCheckinRequestXmlPayload() {
        xmlBuilder = new BaggageCheckinXmlBuilder()
                .withBookingId(java.util.UUID.randomUUID().toString())
                .withPassengerId(java.util.UUID.randomUUID().toString())
                .withBagTag("AB" + String.format("%08d", System.currentTimeMillis() % 100000000))
                .withOrigin("JFK")
                .withDestination("LAX");
        xmlBuilder.withNamespace(xmlNamespace);
        context.setLastRequestBody(xmlBuilder.build());
        ReportLogger.info("Created valid BaggageCheckinRequest XML payload");
    }

    @Given("I use XML namespace {string}")
    public void iUseXmlNamespace(String namespace) {
        this.xmlNamespace = namespace;
        if (xmlBuilder != null) {
            xmlBuilder.withNamespace(namespace);
            context.setLastRequestBody(xmlBuilder.build());
        }
        ReportLogger.info("Set XML namespace: " + namespace);
    }

    @Given("I set XML element {string} to {string}")
    public void iSetXmlElementTo(String elementName, String value) {
        String resolvedValue = resolveVariable(value);
        if (xmlBuilder != null) {
            switch (elementName.toLowerCase()) {
                case "bagtag" -> xmlBuilder.withBagTag(resolvedValue);
                case "bookingid" -> xmlBuilder.withBookingId(resolvedValue);
                case "passengerid" -> xmlBuilder.withPassengerId(resolvedValue);
                case "origin" -> xmlBuilder.withOrigin(resolvedValue);
                case "destination" -> xmlBuilder.withDestination(resolvedValue);
                case "weight" -> xmlBuilder.withWeight(Double.parseDouble(resolvedValue));
            }
            context.setLastRequestBody(xmlBuilder.build());
        }
        ReportLogger.info("Set XML element " + elementName + " = " + resolvedValue);
    }

    @When("I call {string} {string} with XML body")
    public void iCallWithXmlBody(String method, String path) {
        String resolvedPath = resolvePathVariables(path);
        Object body = context.getLastRequestBody();
        Response response = switch (method.toUpperCase()) {
            case "POST" -> context.getClient().post(resolvedPath, context.getHeaders(), body);
            case "PUT" -> context.getClient().put(resolvedPath, context.getHeaders(), body);
            case "PATCH" -> context.getClient().patch(resolvedPath, context.getHeaders(), body);
            default -> throw new IllegalArgumentException("Unsupported method for XML body: " + method);
        };
        context.setLastResponse(response);
    }

    @When("I send XML request to {string}")
    public void iSendXmlRequestTo(String path) {
        // Switch to XML client for XML requests
        context.setXmlClient(framework.config.ServiceType.BAGGAGE);
        iCallWithXmlBody("POST", path);
    }

    @Then("the response should be valid XML")
    public void theResponseShouldBeValidXml() {
        String body = context.getLastResponse().getBody().asString();
        boolean passed = body != null && (body.trim().startsWith("<?xml") || body.trim().startsWith("<"));
        ReportLogger.logAssertion("Response is valid XML", "XML content", 
                passed ? "valid XML" : "not XML", passed);
        assertThat(body)
                .as("Response should be valid XML")
                .matches(s -> s.trim().startsWith("<?xml") || s.trim().startsWith("<"));
    }

    @And("I capture XML element {string} as {string}")
    public void iCaptureXmlElementAs(String elementName, String variableName) {
        String body = context.getLastResponse().getBody().asString();
        String value = extractXmlElement(body, elementName);
        context.set(variableName, value);
        ReportLogger.info("Captured XML element " + variableName + " = " + value);
    }

    @And("the XML element {string} should equal {string}")
    public void theXmlElementShouldEqual(String elementName, String expectedValue) {
        String body = context.getLastResponse().getBody().asString();
        String actualValue = extractXmlElement(body, elementName);
        String resolvedExpected = resolveVariable(expectedValue);
        boolean passed = resolvedExpected.equals(actualValue);
        ReportLogger.logAssertion("XML element " + elementName, resolvedExpected, actualValue, passed);
        assertThat(actualValue)
                .as("XML element %s", elementName)
                .isEqualTo(resolvedExpected);
    }

    @And("the XML error response should be valid")
    public void theXmlErrorResponseShouldBeValid() {
        String body = context.getLastResponse().getBody().asString();
        boolean hasError = body.contains("<error>") || body.contains("<Error>");
        ReportLogger.logAssertion("XML error response valid", "contains error element",
                hasError ? "found" : "not found", hasError);
        assertThat(body)
                .as("XML error response should contain error element")
                .containsIgnoringCase("<error");
    }

    private String extractXmlElement(String xml, String elementName) {
        Pattern pattern = Pattern.compile(
                "<(?:[a-zA-Z0-9]+:)?" + elementName + "[^>]*>([^<]*)</(?:[a-zA-Z0-9]+:)?" + elementName + ">",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String resolveVariable(String value) {
        if (value.startsWith("{") && value.endsWith("}")) {
            String varName = value.substring(1, value.length() - 1);
            String resolved = context.getString(varName);
            return resolved != null ? resolved : value;
        }
        return value;
    }

    private String resolvePathVariables(String path) {
        String resolved = path;
        while (resolved.contains("{")) {
            int start = resolved.indexOf("{");
            int end = resolved.indexOf("}", start);
            if (end == -1) break;
            String varName = resolved.substring(start + 1, end);
            String varValue = context.getString(varName);
            if (varValue != null) {
                resolved = resolved.substring(0, start) + varValue + resolved.substring(end + 1);
            } else {
                break;
            }
        }
        return resolved;
    }
}
