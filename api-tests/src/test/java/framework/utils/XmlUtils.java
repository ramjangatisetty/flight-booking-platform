package framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public final class XmlUtils {
    private static final XmlMapper MAPPER = new XmlMapper();

    private XmlUtils() {}

    public static String toXml(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to XML", e);
        }
    }

    public static String toXml(Object obj, String namespace) {
        String xml = toXml(obj);
        return xml.replaceFirst(">", " xmlns=\"" + namespace + "\">");
    }

    public static <T> T fromXml(String xml, Class<T> type) {
        try {
            return MAPPER.readValue(xml, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize from XML", e);
        }
    }

    public static String wrapWithNamespace(String content, String rootElement, String namespace) {
        return "<%s xmlns=\"%s\">%s</%s>".formatted(rootElement, namespace, content, rootElement);
    }
}
