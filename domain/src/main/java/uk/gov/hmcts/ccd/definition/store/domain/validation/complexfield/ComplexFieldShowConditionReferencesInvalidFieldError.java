package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

public class ComplexFieldShowConditionReferencesInvalidFieldError extends ValidationError {

    private String showConditionField;
    private final ComplexFieldEntity complexField;

    public ComplexFieldShowConditionReferencesInvalidFieldError(String showConditionField,
                                                                ComplexFieldEntity complexField) {
        super(
            String.format(
                "Unknown field '%s' of complex field '%s' in show condition: '%s'",
                showConditionField,
                complexField.getComplexFieldType().getReference(),
                complexField.getShowCondition()
            )
        );
        this.showConditionField = showConditionField;
        this.complexField = complexField;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public String getShowConditionField() {
        return showConditionField;
    }

    public ComplexFieldEntity getComplexField() {
        return complexField;
    }
}
