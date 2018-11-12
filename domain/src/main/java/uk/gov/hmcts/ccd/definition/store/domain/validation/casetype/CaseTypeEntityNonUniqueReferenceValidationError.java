package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

public class CaseTypeEntityNonUniqueReferenceValidationError extends ValidationError {

    private CaseTypeEntity caseTypeEntity;
    private CaseType existingCaseType;

    public CaseTypeEntityNonUniqueReferenceValidationError(CaseTypeEntity caseTypeEntity, CaseType existingCaseType) {
        super(
            String.format(
                "Case Type with reference '%s' already exists for '%s' jurisdiction. Case types must be unique across all existing jurisdictions.",
                caseTypeEntity.getReference(),
                existingCaseType.getJurisdiction().getName()
            )
        );
        this.caseTypeEntity = caseTypeEntity;
        this.existingCaseType = existingCaseType;
    }

    public CaseTypeEntity getCaseTypeEntity() {
        return caseTypeEntity;
    }

    public CaseType getExistingCaseType() {
        return existingCaseType;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
