package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;

public class DisplayGroupInvalidEventFieldShowCondition extends ValidationError {

    private String showConditionField;
    private DisplayGroupEntity displayGroup;

    public DisplayGroupInvalidEventFieldShowCondition(String showConditionField, DisplayGroupEntity displayGroup) {
        super(
            String.format(
                "Invalid show condition '%s' for display group '%s': unknown field '%s' for event '%s'",
                displayGroup.getShowCondition(),
                displayGroup.getReference(),
                showConditionField,
                displayGroup.getEvent().getReference()
            )
        );

        this.showConditionField = showConditionField;
        this.displayGroup = displayGroup;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public String getShowConditionField() {
        return showConditionField;
    }

    public DisplayGroupEntity getDisplayGroup() {
        return displayGroup;
    }
}
