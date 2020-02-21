package uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter;

import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DisplayContextParameterTypeTest {

    private static final String DATE_TIME_ENTRY_PARAMETER = "#DATETIMEENTRY(hhmmss)";

    @Test
    public void shouldGetParameterTypeForKnownType() {
        DisplayContextParameterType result = DisplayContextParameterType.getParameterTypeFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result, is(DisplayContextParameterType.DATETIMEENTRY))
        );
    }

    @Test
    public void shouldFailGetParameterTypeForUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> {
            DisplayContextParameterType.getParameterTypeFor("#INVALID(123)");
        });
    }

    @Test
    public void shouldFailGetParameterTypeForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            DisplayContextParameterType.getParameterTypeFor("INVALID_FORMAT");
        });
    }

    @Test
    public void shouldGetParameterValueForKnownType() {
        String result = DisplayContextParameterType.getParameterValueFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result, is("hhmmss"))
        );
    }

    @Test
    public void shouldFailGetParameterValueForNoValuePassed() {
        assertThrows(IllegalArgumentException.class, () -> {
            DisplayContextParameterType.getParameterValueFor("#DATETIMEENTRY()");
        });
    }

    @Test
    public void shouldFailGetParameterValueForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            DisplayContextParameterType.getParameterTypeFor("(123)");
        });
    }
}
