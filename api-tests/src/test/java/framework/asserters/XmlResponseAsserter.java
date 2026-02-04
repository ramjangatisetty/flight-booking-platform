package framework.asserters;

import io.restassured.response.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public final class XmlResponseAsserter {
    private XmlResponseAsserter() {}

    public static void assertValidXmlResponse(Response response) {
        String body = response.getBody().asString();
        assertThat(body.trim().startsWith("<?xml") || body.trim().startsWith("<"))
                .as("Response should be valid XML")
                .isTrue();
    }

    public static void assertXmlElementPresent(String xml, String elementName) {
        Pattern pattern = Pattern.compile("<" + elementName + "[^>]*>", Pattern.CASE_INSENSITIVE);
        assertThat(pattern.matcher(xml).find())
                .as("XML element '%s' should be present", elementName)
                .isTrue();
    }

    public static void assertXmlElementValue(String xml, String elementName, String expectedValue) {
        Pattern pattern = Pattern.compile(
                "<" + elementName + "[^>]*>([^<]*)</" + elementName + ">",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(xml);
        assertThat(matcher.find())
                .as("XML element '%s' should be present", elementName)
                .isTrue();
        assertThat(matcher.group(1).trim())
                .as("XML element '%s' value", elementName)
                .isEqualTo(expectedValue);
    }
}
