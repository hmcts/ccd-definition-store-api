package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class CaseFieldEntityCORValidationError extends ValidationError {

    private final CaseFieldEntity caseField;

    public CaseFieldEntityCORValidationError(CaseFieldEntity caseField) {

        super(String.format("The Change Organisation Request FieldType must be associated with an ID of "
                + "'ChangeOrganisationRequest' instead of '%s' and may only be defined once in CaseType '%s'",
            caseField.getReference(), caseField.getCaseType().getReference()));
        this.caseField = caseField;
    }

    public CaseFieldEntity getCaseFieldEntity() {
        return caseField;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
