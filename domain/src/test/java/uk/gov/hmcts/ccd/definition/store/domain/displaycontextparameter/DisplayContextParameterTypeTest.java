package uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DisplayContextParameterTypeTest {

    private static final String DATE_TIME_ENTRY_PARAMETER = "#DATETIMEENTRY(HHmmss)";

    @Test
    void shouldGetDisplayContextParameterForValidInput() {
        Optional<DisplayContextParameter> result = DisplayContextParameterType.getDisplayContextParameterFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result.get().getType(), is(DisplayContextParameterType.DATETIMEENTRY)),
            () -> assertThat(result.get().getValue(), is("HHmmss"))
        );
    }

    @Test
    void shouldNotReturnDisplayContextParameterForInvalidType() {
        Optional<DisplayContextParameter> result = DisplayContextParameterType.getDisplayContextParameterFor("#INVALID(123)");

        assertAll(
            () -> assertThat(result.isPresent(), is(false))
        );
    }

    @Test
    void shouldNotReturnDisplayContextParameterForInvalidValue() {
        Optional<DisplayContextParameter> result = DisplayContextParameterType.getDisplayContextParameterFor("#DATETIMEENTRY()");

        assertAll(
            () -> assertThat(result.isPresent(), is(false))
        );
    }

    @Test
    void shouldGetParameterTypeForKnownType() {
        Optional<DisplayContextParameterType> result = DisplayContextParameterType.getParameterTypeFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result.get(), is(DisplayContextParameterType.DATETIMEENTRY))
        );
    }

    @Test
    void shouldNotReturnTypeForUnknownType() {
        Optional<DisplayContextParameterType> result = DisplayContextParameterType.getParameterTypeFor("#INVALID(123)");

        assertAll(
            () -> assertThat(result.isPresent(), is(false))
        );
    }

    @Test
    void shouldNotReturnTypeForInvalidFormat() {
        Optional<DisplayContextParameterType> result = DisplayContextParameterType.getParameterTypeFor("INVALID_FORMAT");

        assertAll(
            () -> assertThat(result.isPresent(), is(false))
        );
    }

    @Test
    void shouldGetParameterValueForKnownType() {
        Optional<String> result = DisplayContextParameterType.getParameterValueFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result.get(), is("HHmmss"))
        );
    }

    @Test
    void shouldNotReturnParameterValueForNoValuePassed() {
        Optional<String> result = DisplayContextParameterType.getParameterValueFor("#DATETIMEENTRY()");

        assertAll(
            () -> assertThat(result.isPresent(), is(false))
        );
    }

    @Test
    void shouldNotReturnParameterValueForInvalidFormat() {
        Optional<String> result = DisplayContextParameterType.getParameterValueFor("(123)");

        assertAll(
            () -> assertThat(result.isPresent(), is(false))
        );
    }
}
