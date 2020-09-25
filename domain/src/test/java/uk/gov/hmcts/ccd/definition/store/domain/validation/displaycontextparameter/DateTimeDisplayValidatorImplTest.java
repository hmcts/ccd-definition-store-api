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
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE_TIME;

public class DateTimeDisplayValidatorImplTest {

    private DateTimeDisplayValidatorImpl validator;

    @Mock
    private DateTimeFormatParser dateTimeFormatParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new DateTimeDisplayValidatorImpl(dateTimeFormatParser);
    }

    @Test
    void shouldBeCorrectType() {
        DisplayContextParameterType result = validator.getType();

        assertThat(result, is(DisplayContextParameterType.DATETIMEDISPLAY));
    }

    @Test
    void shouldValidateValidFormat() throws Exception {
        validator.validate("hhmmss", BASE_DATE_TIME);
    }

    @Test
    void shouldValidateValidFormatWhenFieldIsDate() throws Exception {
        validator.validate("yyyy-MM-dd", BASE_DATE);
    }

    @Test
    void shouldValidateValidFormatWhenFieldIsDateWithSPace() throws Exception {
        validator.validate("yyyy MM dd", BASE_DATE);
    }

    @Test
    void shouldErrorWhenTimeIsConfiguredInDcpForDateField() throws Exception {
        doThrow(InvalidDateTimeFormatException.class).when(dateTimeFormatParser).parseDateTimeFormat(any(), any());
        assertThrows(InvalidDateTimeFormatException.class, () -> validator.validate(
            "yyyy-MM-dd'T'HH:mm:ss", BASE_DATE));
    }

    @Test
    void shouldErrorWhenDateTimeFormatParserErrors() throws Exception {
        doThrow(InvalidDateTimeFormatException.class).when(dateTimeFormatParser).parseDateTimeFormat(any(), any());
        assertThrows(InvalidDateTimeFormatException.class, () -> validator.validate(
            "###", BASE_DATE_TIME));
    }
}
