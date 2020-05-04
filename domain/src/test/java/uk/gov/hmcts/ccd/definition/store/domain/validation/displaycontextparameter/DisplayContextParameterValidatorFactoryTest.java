package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DisplayContextParameterValidatorFactoryTest {

    private DisplayContextParameterValidatorFactory factory;

    @Mock
    private DateTimeFormatParser dateTimeFormatParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCacheValidators() {
        DisplayContextParameterValidator validator1 = new DateTimeDisplayValidatorImpl(dateTimeFormatParser);
        DisplayContextParameterValidator validator2 = new DateTimeEntryValidatorImpl(dateTimeFormatParser);
        factory = new DisplayContextParameterValidatorFactory(Arrays.asList(validator1, validator2));

        DisplayContextParameterValidator result1 = factory.getValidator(DisplayContextParameterType.DATETIMEDISPLAY);
        DisplayContextParameterValidator result2 = factory.getValidator(DisplayContextParameterType.DATETIMEENTRY);

        assertAll(
            () -> assertThat(result1, instanceOf(DateTimeDisplayValidatorImpl.class)),
            () -> assertThat(result2, instanceOf(DateTimeEntryValidatorImpl.class))
        );
    }

    @Test
    void shouldErrorForTypeWithNoValidator() {
        DisplayContextParameterValidator validator = new DateTimeDisplayValidatorImpl(dateTimeFormatParser);
        factory = new DisplayContextParameterValidatorFactory(Collections.singletonList(validator));

        assertThrows(NoSuchElementException.class, () -> {
            factory.getValidator(DisplayContextParameterType.LIST);
        });
    }
}
