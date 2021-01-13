package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class EventEntityShowConditionReferencesInvalidCaseFieldError extends ShowConditionReferencesInvalidCaseFieldError {

    public EventEntityShowConditionReferencesInvalidCaseFieldError(String showConditionField,
                                                                   EventEntity eventEntity,
                                                                   String showCondition) {
        super("Unknown field '%s' for event '%s' in post state condition: '%s'",
            showConditionField,
            eventEntity,
            showCondition
        );
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
