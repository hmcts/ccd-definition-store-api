package uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DisplayContextParameterTest {

    private static final String DATE_TIME_ENTRY_PARAMETER = "#DATETIMEENTRY(HHmmss)";

    @Test
    void shouldGetDisplayContextParameterForValidInput() {
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParametersFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result.get(0).getType(), is(DisplayContextParameterType.DATETIMEENTRY)),
            () -> assertThat(result.get(0).getValue(), is("HHmmss"))
        );
    }

    @Test
    void shouldGetDisplayContextParameterForValidInputForMultipleParameters() {
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParametersFor(DATE_TIME_ENTRY_PARAMETER + ", " + DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result.get(0).getType(), is(DisplayContextParameterType.DATETIMEENTRY)),
            () -> assertThat(result.get(0).getValue(), is("HHmmss")),
            () -> assertThat(result.get(1).getType(), is(DisplayContextParameterType.DATETIMEENTRY)),
            () -> assertThat(result.get(1).getValue(), is("HHmmss"))
        );
    }

    @Test
    void shouldNotReturnDisplayContextParameterForInvalidType() {
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParametersFor("#INVALID(123)");

        assertAll(
            () -> assertThat(result.isEmpty(), is(false))
        );
    }

    @Test
    void shouldNotReturnDisplayContextParameterForInvalidValue() {
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParametersFor("#DATETIMEENTRY()");

        assertAll(
            () -> assertThat(result.isEmpty(), is(false))
        );
    }
}
