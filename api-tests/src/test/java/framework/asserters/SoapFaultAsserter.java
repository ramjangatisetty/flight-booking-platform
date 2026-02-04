package framework.asserters;

import framework.soap.SoapFault;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion utilities for SOAP fault validation.
 */
public final class SoapFaultAsserter {

    private SoapFaultAsserter() {
        // Utility class
    }

    public static void assertValidSoapFault(SoapFault fault) {
        assertThat(fault)
                .as("SOAP fault should not be null")
                .isNotNull();

        assertThat(fault.getFaultCode())
                .as("SOAP fault code should not be null")
                .isNotNull();

        assertThat(fault.getFaultString())
                .as("SOAP fault string should not be null or empty")
                .isNotNull()
                .isNotBlank();
    }

    public static void assertFaultCode(SoapFault fault, String expectedCode) {
        assertThat(fault)
                .as("SOAP fault should not be null")
                .isNotNull();

        assertThat(fault.getFaultCode())
                .as("SOAP fault code should match expected")
                .isEqualTo(expectedCode);
    }

    public static void assertFaultMessageContains(SoapFault fault, String substring) {
        assertThat(fault)
                .as("SOAP fault should not be null")
                .isNotNull();

        String message = fault.getFaultString();
        if (message == null) {
            message = fault.getLoyaltyFaultMessage();
        }

        assertThat(message)
                .as("SOAP fault message should contain: " + substring)
                .containsIgnoringCase(substring);
    }
}
