package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class EventEntityInvalidDefaultPostStateError extends ValidationError {

    private final EventEntity eventEntity;

    public EventEntityInvalidDefaultPostStateError(final EventEntity entity,
                                                   final EventEntityValidationContext context) {
        super(String.format("Non-conditional post state is required for case type '%s', event '%s'",
            context.getCaseReference(),
            entity.getReference()));
        this.eventEntity = entity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }
}
