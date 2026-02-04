package framework.soap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting data from SOAP response envelopes.
 */
public final class SoapResponseParser {

    private static final Pattern BODY_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?Body[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?Body>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FAULT_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?Fault[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?Fault>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FAULT_CODE_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?faultcode[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?faultcode>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FAULT_STRING_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?faultstring[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?faultstring>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DETAIL_PATTERN = Pattern.compile(
            "<(?:[a-zA-Z0-9]+:)?detail[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?detail>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private SoapResponseParser() {
        // Utility class
    }

    public static String extractBody(String envelope) {
        if (envelope == null || envelope.isBlank()) {
            return null;
        }
        Matcher matcher = BODY_PATTERN.matcher(envelope);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static SoapFault extractFault(String envelope) {
        if (envelope == null || envelope.isBlank()) {
            return null;
        }

        Matcher faultMatcher = FAULT_PATTERN.matcher(envelope);
        if (!faultMatcher.find()) {
            return null;
        }

        String faultContent = faultMatcher.group(1);
        SoapFault fault = new SoapFault();

        Matcher codeMatcher = FAULT_CODE_PATTERN.matcher(faultContent);
        if (codeMatcher.find()) {
            fault.setFaultCode(codeMatcher.group(1).trim());
        }

        Matcher stringMatcher = FAULT_STRING_PATTERN.matcher(faultContent);
        if (stringMatcher.find()) {
            fault.setFaultString(stringMatcher.group(1).trim());
        }

        Matcher detailMatcher = DETAIL_PATTERN.matcher(faultContent);
        if (detailMatcher.find()) {
            String detail = detailMatcher.group(1).trim();
            fault.setFaultDetail(detail);
            parseLoyaltyFaultDetail(fault, detail);
        }

        return fault;
    }

    private static void parseLoyaltyFaultDetail(SoapFault fault, String detail) {
        // Extract LoyaltyFault specific fields
        Pattern codePattern = Pattern.compile("<(?:[a-zA-Z0-9]+:)?code[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?code>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Pattern messagePattern = Pattern.compile("<(?:[a-zA-Z0-9]+:)?message[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?message>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher codeMatcher = codePattern.matcher(detail);
        if (codeMatcher.find()) {
            fault.setLoyaltyFaultCode(codeMatcher.group(1).trim());
        }

        Matcher messageMatcher = messagePattern.matcher(detail);
        if (messageMatcher.find()) {
            fault.setLoyaltyFaultMessage(messageMatcher.group(1).trim());
        }
    }

    public static <T> T extractElement(String envelope, String elementName, Class<T> type) {
        if (envelope == null || elementName == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                "<(?:[a-zA-Z0-9]+:)?" + elementName + "[^>]*>(.*?)</(?:[a-zA-Z0-9]+:)?" + elementName + ">",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(envelope);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            return convertToType(value, type);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertToType(String value, Class<T> type) {
        if (type == String.class) {
            return (T) value;
        } else if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(value);
        }
        return (T) value;
    }
}
