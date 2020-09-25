package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class EventEntityShowConditionReferencesInvalidCaseFieldError extends ValidationError {

    private EventEntity eventEntity;

    private String showConditionField;

    public EventEntityShowConditionReferencesInvalidCaseFieldError(String showConditionField,
                                                                   EventEntity eventEntity,
                                                                   String showCondition) {
        super(
            String.format(
                "Unknown field '%s' for event '%s' in post state condition: '%s'",
                showConditionField,
                eventEntity.getReference(),
                showCondition
            )
        );
        this.showConditionField = showConditionField;
        this.eventEntity = eventEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public String getShowConditionField() {
        return showConditionField;
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }
}
