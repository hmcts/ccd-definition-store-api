package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class CreateEventDoesNotHavePostStateValidationError extends ValidationError {

    private EventEntity eventEntity;

    public CreateEventDoesNotHavePostStateValidationError(EventEntity eventEntity) {
        super(String.format(
            "PostState must be defined for the event with reference '%s'",
            eventEntity.getReference()
            )
        );
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
