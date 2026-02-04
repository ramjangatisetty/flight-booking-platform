package framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for XML serialization and deserialization.
 */
public final class XmlUtils {

    private static final XmlMapper XML_MAPPER = (XmlMapper) new XmlMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private XmlUtils() {
        // Utility class
    }

    public static String toXml(Object obj) {
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to XML", e);
        }
    }

    public static String toXml(Object obj, String namespace) {
        String xml = toXml(obj);
        // Add namespace to root element if not present
        if (namespace != null && !namespace.isBlank() && !xml.contains("xmlns=")) {
            int firstTagEnd = xml.indexOf('>');
            if (firstTagEnd > 0) {
                xml = xml.substring(0, firstTagEnd) + " xmlns=\"" + namespace + "\"" + xml.substring(firstTagEnd);
            }
        }
        return xml;
    }

    public static <T> T fromXml(String xml, Class<T> type) {
        try {
            return XML_MAPPER.readValue(xml, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize XML to " + type.getSimpleName(), e);
        }
    }

    public static String wrapWithNamespace(String content, String rootElement, String namespace) {
        return String.format("<%s xmlns=\"%s\">%s</%s>", rootElement, namespace, content, rootElement);
    }

    public static XmlMapper getMapper() {
        return XML_MAPPER;
    }
}
