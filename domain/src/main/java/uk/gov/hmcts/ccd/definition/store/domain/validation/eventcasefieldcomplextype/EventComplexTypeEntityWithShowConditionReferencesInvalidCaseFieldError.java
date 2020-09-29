package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

public class EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError extends ValidationError {

    private EventComplexTypeEntity eventComplexTypeEntity;

    private String showConditionField;

    private String eventId;

    public EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError(
        String showConditionField,
        EventCaseFieldEntityValidationContext validationContext,
        EventComplexTypeEntity eventComplexTypeEntity) {
        super(
            String.format(
                "Unknown field '%s' for event '%s' in show condition: '%s'",
                showConditionField,
                validationContext.getEventId(),
                eventComplexTypeEntity.getShowCondition()));

        this.eventComplexTypeEntity = eventComplexTypeEntity;
        this.showConditionField = showConditionField;
        this.eventId = validationContext.getEventId();
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventComplexTypeEntity getEventComplexTypeEntity() {
        return eventComplexTypeEntity;
    }

    public String getShowConditionField() {
        return showConditionField;
    }

    public String getEventId() {
        return eventId;
    }

}
