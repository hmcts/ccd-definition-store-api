package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class CaseFieldEntityInvalidMetadataFieldValidationError extends ValidationError {

    private final CaseFieldEntity metadataField;

    public CaseFieldEntityInvalidMetadataFieldValidationError(String message, CaseFieldEntity caseFieldEntity,
                                                              CaseFieldEntityValidationContext context) {
        super(message);
        this.metadataField = caseFieldEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public CaseFieldEntity getMetadataField() {
        return metadataField;
    }
}
