package framework.asserters;

import framework.models.common.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;

public final class ErrorAsserter {
    private ErrorAsserter() {}

    public static void assertValidErrorResponse(ErrorResponse error) {
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isGreaterThan(0);
        assertThat(error.getError()).isNotBlank();
    }

    public static void assertStatusCode(ErrorResponse error, int expectedStatus) {
        assertThat(error.getStatus())
                .as("Error status code")
                .isEqualTo(expectedStatus);
    }

    public static void assertCorrelationId(ErrorResponse error, String expectedCorrelationId) {
        assertThat(error.getCorrelationId())
                .as("Correlation ID in error response")
                .isEqualTo(expectedCorrelationId);
    }

    public static void assertMessageContains(ErrorResponse error, String substring) {
        assertThat(error.getMessage())
                .as("Error message")
                .containsIgnoringCase(substring);
    }
}
