package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class EventEntityInvalidPostStatePriorityError extends ValidationError {

    private final EventEntity eventEntity;

    public EventEntityInvalidPostStatePriorityError(final EventEntity eventEntity,
                                                    final EventEntityValidationContext context) {
        super(String.format("Duplicate post state priorities for case type '%s', event '%s'",
            context.getCaseReference(),
            eventEntity.getReference()));
        this.eventEntity = eventEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }
}
