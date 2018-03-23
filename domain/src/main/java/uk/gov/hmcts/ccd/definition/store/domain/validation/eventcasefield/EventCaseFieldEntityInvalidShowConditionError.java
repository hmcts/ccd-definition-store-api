package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

public class EventCaseFieldEntityInvalidShowConditionError extends ValidationError {

    private final EventCaseFieldEntityValidationContext validationContext;
    private EventCaseFieldEntity eventCaseFieldEntity;

    public EventCaseFieldEntityInvalidShowConditionError(EventCaseFieldEntity eventCaseFieldEntity,
                                                         EventCaseFieldEntityValidationContext validationContext) {
        super(
            String.format(
                "Show condition '%s' invalid for event '%s'",
                eventCaseFieldEntity.getShowCondition(),
                validationContext.getEventId()
            )
        );
        this.eventCaseFieldEntity = eventCaseFieldEntity;
        this.validationContext = validationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventCaseFieldEntity getEventCaseFieldEntity() {
        return this.eventCaseFieldEntity;
    }

    public EventCaseFieldEntityValidationContext getValidationContext() {
        return this.validationContext;
    }

}
