package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CaseTypeEntityMissingSecurityClassificationValidationError extends ValidationError {

    private CaseTypeEntity caseTypeEntity;

    public CaseTypeEntityMissingSecurityClassificationValidationError(CaseTypeEntity caseTypeEntity) {
        super(
            String.format(
                "Case Type with name '%s' must have a Security Classification defined",
                caseTypeEntity.getName()
            )
        );
        this.caseTypeEntity = caseTypeEntity;
    }

    public CaseTypeEntity getCaseTypeEntity() {
        return caseTypeEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
