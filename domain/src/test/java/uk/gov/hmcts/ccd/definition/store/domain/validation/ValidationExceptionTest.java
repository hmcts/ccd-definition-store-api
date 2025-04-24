package uk.gov.hmcts.ccd.definition.store.domain.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ValidationExceptionTest {


    @Test
    public void getMessage_shouldContainValidationResult() {
        final ValidationResult validationResult = new ValidationResult();
        final ValidationException exception = new ValidationException(validationResult);
        assertEquals(validationResult, exception.getValidationResult());
    }

}
