package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

public class LabelTypeCannotBeEditableValidationError extends ValidationError {

    private EventCaseFieldEntity eventCaseFieldEntity;

    public LabelTypeCannotBeEditableValidationError(EventCaseFieldEntity eventCaseFieldEntity) {
        super(String.format(
                "'%s' is Label type and cannot be editable for event with reference '%s'",
                eventCaseFieldEntity.getCaseField().getReference(),
                eventCaseFieldEntity.getEvent().getReference()
                )
            );
        this.eventCaseFieldEntity = eventCaseFieldEntity;
    }

    public EventCaseFieldEntity getEventCaseFieldEntity() {
        return eventCaseFieldEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
