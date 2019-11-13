package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public class ComplexFieldEntityMissingOrderValidationError extends ValidationError {

    private FieldEntity fieldEntity;

    public ComplexFieldEntityMissingOrderValidationError(FieldEntity complexFieldEntity) {
        super(String.format("ComplexField with reference '%s' must have ordering for all children defined", complexFieldEntity.getReference()));
        this.fieldEntity = complexFieldEntity;
    }

    public FieldEntity getFieldEntity() {
        return fieldEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
