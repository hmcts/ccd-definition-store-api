package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

public class ComplexFieldInvalidShowConditionError extends ValidationError {

    private ComplexFieldEntity complexField;

    public ComplexFieldInvalidShowConditionError(ComplexFieldEntity complexField) {
        super(
            String.format(
                "Show condition '%s' invalid for complex field element '%s'",
                complexField.getShowCondition(),
                complexField.getReference()
            )
        );
        this.complexField = complexField;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public ComplexFieldEntity getComplexField() {
        return complexField;
    }
}
