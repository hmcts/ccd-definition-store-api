package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class CaseFieldEntityCORValidationError extends ValidationError {

    private final CaseFieldEntity caseField;

    public CaseFieldEntityCORValidationError(CaseFieldEntity caseField) {

        super(String.format("Change Organisation Request is defined more than once for case type '%s'. ",
            defaultString(caseField.getCaseType().getReference())));
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
