package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

public class ComplexFieldEntityIncorrectOrderValidationError extends SimpleValidationError {


    public ComplexFieldEntityIncorrectOrderValidationError(ComplexFieldEntity complexFieldEntity) {
        super(String.format(
            "ComplexField with reference=%s has incorrect order for nested fields. "
                + "Order has to be incremental and start from 1",
            complexFieldEntity.getReference()),
            complexFieldEntity);
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
