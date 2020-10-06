package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class CaseFieldEntityMissingSecurityClassificationValidationError extends ValidationError {

    private CaseFieldEntity caseFieldEntity;

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    public CaseFieldEntityMissingSecurityClassificationValidationError(
        CaseFieldEntity caseFieldEntity, CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        super(String.format(
            "CaseField with reference '%s' must have a Security Classification defined",
            caseFieldEntity.getReference()));
        this.caseFieldEntity = caseFieldEntity;
        this.caseFieldEntityValidationContext = caseFieldEntityValidationContext;
    }

    public CaseFieldEntity getCaseFieldEntity() {
        return caseFieldEntity;
    }

    public CaseFieldEntityValidationContext getCaseFieldEntityValidationContext() {
        return caseFieldEntityValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
