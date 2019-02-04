package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

public class EventComplexTypeEntityInvalidShowConditionError extends ValidationError {

    private final EventCaseFieldEntityValidationContext validationContext;
    private EventComplexTypeEntity eventComplexTypeEntity;

    public EventComplexTypeEntityInvalidShowConditionError(EventComplexTypeEntity eventComplexTypeEntity,
                                                           EventCaseFieldEntityValidationContext validationContext) {
        super(
            String.format(
                "Show condition '%s' invalid for event '%s'",
                eventComplexTypeEntity.getShowCondition(),
                validationContext.getEventId()
            )
        );
        this.eventComplexTypeEntity = eventComplexTypeEntity;
        this.validationContext = validationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventComplexTypeEntity getEventCaseFieldEntity() {
        return this.eventComplexTypeEntity;
    }

    public EventCaseFieldEntityValidationContext getValidationContext() {
        return this.validationContext;
    }

}
