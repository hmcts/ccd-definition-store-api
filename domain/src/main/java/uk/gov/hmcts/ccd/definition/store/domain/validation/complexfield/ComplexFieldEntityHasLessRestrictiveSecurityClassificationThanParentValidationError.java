package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.CaseFieldComplexFieldEntityValidator.ValidationContext;

public class ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
    extends ValidationError {

    private ComplexFieldEntity complexFieldEntity;

    private ValidationContext complexFieldEntityValidationContext;

    public ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
        ComplexFieldEntity complexFieldEntity, ValidationContext validationContext) {
        super(String.format(
            "Security classification for ComplexField with reference '%s' "
                + "has a less restrictive security classification of '%s' than its parent CaseField '%s' "
                + "which is '%s'.",
            complexFieldEntity.getReference(),
            complexFieldEntity.getSecurityClassification(),
            validationContext.getCaseFieldReference(),
            validationContext.getParentSecurityClassification()
            )
        );
        this.complexFieldEntity = complexFieldEntity;
        this.complexFieldEntityValidationContext = validationContext;
    }

    public ComplexFieldEntity getComplexFieldEntity() {
        return complexFieldEntity;
    }

    public ValidationContext getComplexFieldEntityValidationContext() {
        return complexFieldEntityValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
