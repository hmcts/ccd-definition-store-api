package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class EventEntityMissingSecurityClassificationValidationError extends ValidationError {

    private EventEntity eventEntity;


    public EventEntityMissingSecurityClassificationValidationError(EventEntity eventEntity) {
        super(String.format(
            "CaseField with reference '%s' must have a Security Classification defined", eventEntity.getReference()));
        this.eventEntity = eventEntity;
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
