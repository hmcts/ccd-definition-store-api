package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;

public class DisplayGroupInvalidTabShowCondition extends ValidationError {

    private String showConditionField;
    private DisplayGroupEntity displayGroup;

    public DisplayGroupInvalidTabShowCondition(String showConditionField, DisplayGroupEntity displayGroup) {
        super(
            String.format(
                "Invalid show condition '%s' for tab '%s': unknown field '%s'",
                displayGroup.getShowCondition(),
                displayGroup.getReference(),
                showConditionField
            )
        );

        this.showConditionField = showConditionField;
        this.displayGroup = displayGroup;
    }

    public DisplayGroupInvalidTabShowCondition(DisplayGroupEntity displayGroup) {
        super(
            String.format(
                "Unable to parse show condition '%s' for tab '%s'",
                displayGroup.getShowCondition(),
                displayGroup.getReference()
            )
        );

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
