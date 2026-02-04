package framework.mappers;

import framework.utils.XmlUtils;
import io.restassured.response.Response;

/**
 * Mapper for converting XML responses to model objects.
 */
public final class XmlResponseMapper {

    private XmlResponseMapper() {
        // Utility class
    }

    public static <T> T fromXml(String xml, Class<T> type) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            return XmlUtils.fromXml(xml, type);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T fromResponse(Response response, Class<T> type) {
        if (response == null || response.getBody() == null) {
            return null;
        }
        return fromXml(response.getBody().asString(), type);
    }
}
