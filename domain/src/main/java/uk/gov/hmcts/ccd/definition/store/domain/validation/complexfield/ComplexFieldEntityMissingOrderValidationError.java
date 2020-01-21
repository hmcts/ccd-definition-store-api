package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

public class ComplexFieldEntityMissingOrderValidationError extends SimpleValidationError {

    public ComplexFieldEntityMissingOrderValidationError(ComplexFieldEntity complexFieldEntity) {
        super(String.format("ComplexField with reference=%s must have ordering for all children defined", complexFieldEntity.getReference()),
              complexFieldEntity);
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
