package framework.asserters;

import io.restassured.response.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion utilities for XML response validation.
 */
public final class XmlResponseAsserter {

    private XmlResponseAsserter() {
        // Utility class
    }

    public static void assertValidXmlResponse(Response response) {
        assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        String contentType = response.getContentType();
        assertThat(contentType)
                .as("Content-Type should be XML")
                .containsIgnoringCase("xml");

        String body = response.getBody().asString();
        assertThat(body)
                .as("Response body should not be empty")
                .isNotBlank();

        assertThat(body)
                .as("Response body should be valid XML (starts with < or <?xml)")
                .matches(s -> s.trim().startsWith("<"));
    }

    public static void assertXmlElementPresent(String xml, String elementName) {
        assertThat(xml)
                .as("XML should not be null")
                .isNotNull();

        Pattern pattern = Pattern.compile(
                "<(?:[a-zA-Z0-9]+:)?" + elementName + "[^>]*>",
                Pattern.CASE_INSENSITIVE
        );

        assertThat(pattern.matcher(xml).find())
                .as("XML should contain element: " + elementName)
                .isTrue();
    }

    public static void assertXmlElementValue(String xml, String elementName, String expectedValue) {
        assertThat(xml)
                .as("XML should not be null")
                .isNotNull();

        Pattern pattern = Pattern.compile(
                "<(?:[a-zA-Z0-9]+:)?" + elementName + "[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?" + elementName + ">",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(xml);
        assertThat(matcher.find())
                .as("XML should contain element: " + elementName)
                .isTrue();

        String actualValue = matcher.group(1).trim();
        assertThat(actualValue)
                .as("Element " + elementName + " should have value: " + expectedValue)
                .isEqualTo(expectedValue);
    }
}
