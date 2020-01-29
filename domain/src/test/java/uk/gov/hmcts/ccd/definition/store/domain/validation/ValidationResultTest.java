package uk.gov.hmcts.ccd.definition.store.domain.validation;

import org.junit.jupiter.api.Test;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

class ValidationResultTest {

    @Test
    void shouldAddNewError() {

        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(new ValidationError("Error") {
            @Override
            public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
                return super.createMessage(validationErrorMessageCreator);
            }
        });

        ValidationResult newValidationResult = new ValidationResult();
        newValidationResult.addError(new ValidationError("Error2") {
            @Override
            public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
                return super.createMessage(validationErrorMessageCreator);
            }
        });
        validationResult.merge(newValidationResult);

        assertThat(validationResult.getValidationErrors(), hasSize(2));
    }

    @Test
    void shouldNotAddNewErrorIfMessageSame() {

        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(new ValidationError("Error") {
            @Override
            public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
                return super.createMessage(validationErrorMessageCreator);
            }
        });

        ValidationResult newValidationResult = new ValidationResult();
        newValidationResult.addError(new ValidationError("Error") {
            @Override
            public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
                return super.createMessage(validationErrorMessageCreator);
            }
        });
        validationResult.merge(newValidationResult);

        assertThat(validationResult.getValidationErrors(), hasSize(1));
    }
}
