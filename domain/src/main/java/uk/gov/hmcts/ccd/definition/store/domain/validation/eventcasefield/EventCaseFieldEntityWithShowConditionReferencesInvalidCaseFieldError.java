package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

public class EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError extends ValidationError {

    private EventCaseFieldEntity eventCaseFieldEntity;

    private String showConditionField;

    private String eventId;

    public EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
        String showConditionField,
        EventCaseFieldEntityValidationContext validationContext,
        EventCaseFieldEntity eventCaseFieldEntity) {
        super(
            String.format(
                "Unknown field '%s' for event '%s' in show condition: '%s'",
                showConditionField,
                validationContext.getEventId(),
                eventCaseFieldEntity.getShowCondition()
            )
        );
        this.eventCaseFieldEntity = eventCaseFieldEntity;
        this.showConditionField = showConditionField;
        this.eventId = validationContext.getEventId();
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventCaseFieldEntity getEventCaseFieldEntity() {
        return eventCaseFieldEntity;
    }

    public String getShowConditionField() {
        return showConditionField;
    }

    public String getEventId() {
        return eventId;
    }

}
