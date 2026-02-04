package framework.mappers;

import framework.models.common.ErrorResponse;
import framework.utils.JsonUtils;
import io.restassured.response.Response;

/**
 * Mapper for converting responses to ErrorResponse model.
 */
public final class ErrorResponseMapper {

    private ErrorResponseMapper() {
        // Utility class
    }

    public static ErrorResponse fromResponse(Response response) {
        if (response == null || response.getBody() == null) {
            return null;
        }
        return fromJson(response.getBody().asString());
    }

    public static ErrorResponse fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JsonUtils.fromJson(json, ErrorResponse.class);
        } catch (Exception e) {
            return null;
        }
    }
}
