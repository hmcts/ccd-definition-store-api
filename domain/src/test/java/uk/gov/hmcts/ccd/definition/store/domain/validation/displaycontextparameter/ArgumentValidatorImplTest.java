package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;


public class ArgumentValidatorImplTest {

    private ArgumentValidatorImpl validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new ArgumentValidatorImpl();
    }

    @Test
    void shouldBeCorrectType() {
        DisplayContextParameterType result = validator.getType();

        assertThat(result, is(DisplayContextParameterType.ARGUMENT));
    }

    @Test
    void shouldValidateValidFormat() throws Exception {
        validator.validate("TestArgument", BASE_TEXT);
    }

}
