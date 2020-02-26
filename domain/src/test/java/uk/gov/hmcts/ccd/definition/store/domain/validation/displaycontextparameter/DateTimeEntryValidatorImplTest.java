package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class DateTimeEntryValidatorImplTest {

    private DateTimeEntryValidatorImpl validator;

    @Mock
    private DateTimeFormatParser dateTimeFormatParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new DateTimeEntryValidatorImpl(dateTimeFormatParser);
    }

    @Test
    void shouldBeCorrectType() {
        DisplayContextParameterType result = validator.getType();

        assertThat(result, is(DisplayContextParameterType.DATETIMEENTRY));
    }

    @Test
    void shouldValidateValidFormat() throws Exception {
        validator.validate("HHmmss");
    }

    @Test
    void shouldErrorWhenDateTimeFormatParserErrors() throws Exception {
        doThrow(InvalidDateTimeFormatException.class).when(dateTimeFormatParser).parseDateTimeFormat(any(), any());
        assertThrows(InvalidDateTimeFormatException.class, () -> validator.validate("###"));
    }
}
