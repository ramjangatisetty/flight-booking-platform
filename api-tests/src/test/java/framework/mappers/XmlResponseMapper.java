package framework.mappers;

import framework.utils.XmlUtils;
import io.restassured.response.Response;

public final class XmlResponseMapper {
    private XmlResponseMapper() {}

    public static <T> T fromXml(String xml, Class<T> type) {
        return XmlUtils.fromXml(xml, type);
    }

    public static <T> T fromResponse(Response response, Class<T> type) {
        return fromXml(response.getBody().asString(), type);
    }
}
