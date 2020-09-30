package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError extends ValidationError {

    private CaseFieldEntity caseFieldEntity;

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    public CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
        CaseFieldEntity caseFieldEntity, CaseFieldEntityValidationContext caseFieldValidationContext) {
        super(String.format(
            "Security classification for CaseField with reference '%s' "
                + "has a less restrictive security classification of '%s' than its parent CaseType '%s' "
                + "which is '%s'.",
            caseFieldEntity.getReference(),
            caseFieldEntity.getSecurityClassification(),
            caseFieldValidationContext.getCaseName(),
            caseFieldValidationContext.getParentSecurityClassification()
            )
        );
        this.caseFieldEntity = caseFieldEntity;
        this.caseFieldEntityValidationContext = caseFieldValidationContext;
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
