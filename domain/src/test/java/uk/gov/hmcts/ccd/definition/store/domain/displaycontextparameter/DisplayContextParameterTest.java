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
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParameterFor(DATE_TIME_ENTRY_PARAMETER);

        assertAll(
            () -> assertThat(result.get(0).getType(), is(DisplayContextParameterType.DATETIMEENTRY)),
            () -> assertThat(result.get(0).getValue(), is("HHmmss"))
        );
    }

    @Test
    void shouldNotReturnDisplayContextParameterForInvalidType() {
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParameterFor("#INVALID(123)");

        assertAll(
            () -> assertThat(result.isEmpty(), is(false))
        );
    }

    @Test
    void shouldNotReturnDisplayContextParameterForInvalidValue() {
        List<DisplayContextParameter> result = DisplayContextParameter.getDisplayContextParameterFor("#DATETIMEENTRY()");

        assertAll(
            () -> assertThat(result.isEmpty(), is(false))
        );
    }
}
