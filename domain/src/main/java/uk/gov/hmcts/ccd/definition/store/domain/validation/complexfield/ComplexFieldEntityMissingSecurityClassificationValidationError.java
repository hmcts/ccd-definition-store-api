package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

public class ComplexFieldEntityMissingSecurityClassificationValidationError extends ValidationError {

    private ComplexFieldEntity complexFieldEntity;

    public ComplexFieldEntityMissingSecurityClassificationValidationError(ComplexFieldEntity complexFieldEntity) {
        super(String.format(
            "ComplexField with reference '%s' must have a Security Classification defined",
            complexFieldEntity.getReference()));
        this.complexFieldEntity = complexFieldEntity;
    }

    public ComplexFieldEntity getComplexFieldEntity() {
        return complexFieldEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
