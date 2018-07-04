package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class CaseFieldEntityInvalidMetadataFieldValidationError extends ValidationError {

    private final CaseFieldEntity metadataField;

    public CaseFieldEntityInvalidMetadataFieldValidationError(CaseFieldEntity caseFieldEntity,
                                                              CaseFieldEntityValidationContext context) {
        super(String.format("Invalid metadata field '%s' declaration for case type '%s'",
                            caseFieldEntity.getReference(),
                            context.getCaseReference()));
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
