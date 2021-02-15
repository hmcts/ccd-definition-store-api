package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;

public class EventEntityCaseTypeUsersValidationError extends ValidationError {
    private EventACLEntity entity;

    public EventEntityCaseTypeUsersValidationError(final EventACLEntity entity) {
        super(String.format("UserRole '%s' can only be associated once with CaseType '%s' for event '%s'",
            entity.getUserRoleId(), entity.getEvent().getCaseType().getReference(), entity.getEvent().getName()));
        this.entity = entity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventACLEntity getEventACLEntity() {
        return entity;
    }


}
