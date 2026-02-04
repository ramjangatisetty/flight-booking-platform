package framework.asserters;

import framework.soap.SoapFault;

import static org.assertj.core.api.Assertions.assertThat;

public final class SoapFaultAsserter {
    private SoapFaultAsserter() {}

    public static void assertValidSoapFault(SoapFault fault) {
        assertThat(fault).isNotNull();
        assertThat(fault.getFaultCode()).isNotBlank();
    }

    public static void assertFaultCode(SoapFault fault, String expectedCode) {
        assertThat(fault.getFaultCode())
                .as("SOAP fault code")
                .isEqualTo(expectedCode);
    }

    public static void assertFaultMessageContains(SoapFault fault, String substring) {
        assertThat(fault.getFaultString())
                .as("SOAP fault message")
                .containsIgnoringCase(substring);
    }
}
