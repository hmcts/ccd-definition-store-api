package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CaseTypeEntityNonUniqueReferenceValidationError extends ValidationError {

    private CaseTypeEntity caseTypeEntity;
    private String existingJurisdictionName;

    public CaseTypeEntityNonUniqueReferenceValidationError(CaseTypeEntity caseTypeEntity, String existingJurisdictionName) {
        super(
            String.format(
                "Case Type with reference '%s' already exists for '%s' jurisdiction. Case types must be unique across all existing jurisdictions.",
                caseTypeEntity.getReference(),
                existingJurisdictionName
            )
        );
        this.caseTypeEntity = caseTypeEntity;
        this.existingJurisdictionName = existingJurisdictionName;
    }

    public CaseTypeEntity getCaseTypeEntity() {
        return caseTypeEntity;
    }

    public String getExistingJurisdictionName() {
        return existingJurisdictionName;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
