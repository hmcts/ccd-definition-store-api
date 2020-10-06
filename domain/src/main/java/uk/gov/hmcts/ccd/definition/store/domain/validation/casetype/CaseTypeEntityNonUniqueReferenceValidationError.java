package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CaseTypeEntityNonUniqueReferenceValidationError extends ValidationError {

    private CaseTypeEntity caseTypeEntity;

    public CaseTypeEntityNonUniqueReferenceValidationError(CaseTypeEntity caseTypeEntity) {
        super(
            String.format(
                "Case Type with reference '%s' already exists. "
                    + "Case types must be unique across all existing jurisdictions.",
                caseTypeEntity.getReference()
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
