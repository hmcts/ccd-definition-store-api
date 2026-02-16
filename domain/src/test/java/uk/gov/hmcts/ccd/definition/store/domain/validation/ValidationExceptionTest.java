package uk.gov.hmcts.ccd.definition.store.domain.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


class ValidationExceptionTest {


    @Test
    void getMessage_shouldContainValidationResult() {
        final ValidationResult validationResult = new ValidationResult();
        final ValidationException exception = new ValidationException(validationResult);
        assertEquals(validationResult, exception.getValidationResult());
    }

}
