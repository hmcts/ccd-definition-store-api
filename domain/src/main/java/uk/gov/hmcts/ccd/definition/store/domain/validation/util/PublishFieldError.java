package uk.gov.hmcts.ccd.definition.store.domain.validation.util;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;

public class PublishFieldError extends ValidationError {

    public PublishFieldError(String message) {
        super(message);
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
