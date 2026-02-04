package framework.asserters;

import framework.models.common.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion utilities for ErrorResponse validation.
 */
public final class ErrorAsserter {

    private ErrorAsserter() {
        // Utility class
    }

    public static void assertValidErrorResponse(ErrorResponse error) {
        assertThat(error)
                .as("ErrorResponse should not be null")
                .isNotNull();

        assertThat(error.getTimestamp())
                .as("ErrorResponse timestamp should not be null")
                .isNotNull();

        assertThat(error.getStatus())
                .as("ErrorResponse status should be a valid HTTP status code")
                .isGreaterThanOrEqualTo(400)
                .isLessThan(600);

        assertThat(error.getError())
                .as("ErrorResponse error should not be null or empty")
                .isNotNull()
                .isNotBlank();

        assertThat(error.getMessage())
                .as("ErrorResponse message should not be null")
                .isNotNull();

        assertThat(error.getPath())
                .as("ErrorResponse path should not be null or empty")
                .isNotNull()
                .isNotBlank();
    }

    public static void assertStatusCode(ErrorResponse error, int expectedStatus) {
        assertThat(error)
                .as("ErrorResponse should not be null")
                .isNotNull();

        assertThat(error.getStatus())
                .as("ErrorResponse status should match expected")
                .isEqualTo(expectedStatus);
    }

    public static void assertCorrelationId(ErrorResponse error, String expectedCorrelationId) {
        assertThat(error)
                .as("ErrorResponse should not be null")
                .isNotNull();

        assertThat(error.getCorrelationId())
                .as("ErrorResponse correlationId should match expected")
                .isEqualTo(expectedCorrelationId);
    }

    public static void assertMessageContains(ErrorResponse error, String substring) {
        assertThat(error)
                .as("ErrorResponse should not be null")
                .isNotNull();

        assertThat(error.getMessage())
                .as("ErrorResponse message should contain: " + substring)
                .containsIgnoringCase(substring);
    }
}
