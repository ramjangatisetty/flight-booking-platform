package framework.soap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoapResponseParser {
    private static final Pattern BODY_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?Body[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?Body>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FAULT_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?Fault[^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    public static String extractBody(String envelope) {
        if (envelope == null) return null;
        Matcher matcher = BODY_PATTERN.matcher(envelope);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static SoapFault extractFault(String envelope) {
        if (envelope == null || !FAULT_PATTERN.matcher(envelope).find()) {
            return null;
        }
        String faultCode = extractElement(envelope, "faultcode");
        String faultString = extractElement(envelope, "faultstring");
        String faultDetail = extractElement(envelope, "detail");
        String loyaltyFaultCode = extractElement(envelope, "code");
        String loyaltyFaultMessage = extractElement(envelope, "message");
        return new SoapFault(faultCode, faultString, faultDetail, loyaltyFaultCode, loyaltyFaultMessage);
    }

    public static String extractElement(String xml, String elementName) {
        if (xml == null) return null;
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

    @SuppressWarnings("unchecked")
    public static <T> T extractElement(String envelope, String elementName, Class<T> type) {
        String value = extractElement(envelope, elementName);
        if (value == null) return null;
        if (type == String.class) return (T) value;
        if (type == Integer.class) return (T) Integer.valueOf(value);
        if (type == Long.class) return (T) Long.valueOf(value);
        if (type == Double.class) return (T) Double.valueOf(value);
        if (type == Boolean.class) return (T) Boolean.valueOf(value);
        return (T) value;
    }
}
