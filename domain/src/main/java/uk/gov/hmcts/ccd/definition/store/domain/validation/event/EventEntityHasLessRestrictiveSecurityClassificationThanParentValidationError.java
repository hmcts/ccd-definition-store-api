package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError extends ValidationError {

    private EventEntity eventEntity;

    private EventEntityValidationContext eventEntityValidationContext;

    public EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
        EventEntity eventEntity, EventEntityValidationContext eventEntityValidationContext) {
        super(String.format(
            "Security classification for Event with reference '%s' "
                + "has a less restrictive security classification of '%s' than its parent CaseType '%s' "
                + "which is '%s'.",
            eventEntity.getReference(),
            eventEntity.getSecurityClassification(),
            eventEntityValidationContext.getCaseName(),
            eventEntityValidationContext.getParentSecurityClassification()
            )
        );
        this.eventEntity = eventEntity;
        this.eventEntityValidationContext = eventEntityValidationContext;
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }

    public EventEntityValidationContext getEventEntityValidationContext() {
        return eventEntityValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
