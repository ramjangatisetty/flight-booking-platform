package framework.mappers;

import framework.models.common.ErrorResponse;
import framework.utils.JsonUtils;
import io.restassured.response.Response;

public final class ErrorResponseMapper {
    private ErrorResponseMapper() {}

    public static ErrorResponse fromResponse(Response response) {
        return fromJson(response.getBody().asString());
    }

    public static ErrorResponse fromJson(String json) {
        return JsonUtils.fromJson(json, ErrorResponse.class);
    }
}
