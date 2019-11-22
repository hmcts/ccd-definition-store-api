package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class ComplexFieldEntityMissingOrderValidationError extends SimpleValidationError {

    public ComplexFieldEntityMissingOrderValidationError(FieldTypeEntity fieldTypeEntity) {
        super(String.format("ComplexField with reference=%s must have ordering for all children defined. WorkSheet 'ComplexTypes'",
                            fieldTypeEntity.isCollectionFieldType() ?
                                fieldTypeEntity.getCollectionFieldType().getReference() :
                                fieldTypeEntity.getReference()),
              fieldTypeEntity);
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
