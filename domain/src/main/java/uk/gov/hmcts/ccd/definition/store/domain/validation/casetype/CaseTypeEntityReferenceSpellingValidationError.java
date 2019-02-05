package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CaseTypeEntityReferenceSpellingValidationError extends ValidationError {

    private String definitiveCaseTypeReference;
    private CaseTypeEntity caseTypeEntity;

    public CaseTypeEntityReferenceSpellingValidationError(final String definitiveCaseTypeReference,
                                                          final CaseTypeEntity caseTypeEntity) {
        super(
            String.format(
                "Definitive spelling of this Case Type ID is '%s' but the imported Case Type ID was '%s'.",
                definitiveCaseTypeReference, caseTypeEntity.getReference()
            )
        );
        this.definitiveCaseTypeReference = definitiveCaseTypeReference;
        this.caseTypeEntity = caseTypeEntity;
    }

    public String getDefinitiveCaseTypeReference() {
        return definitiveCaseTypeReference;
    }

    public CaseTypeEntity getCaseTypeEntity() {
        return caseTypeEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
