package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;

public class EventEntityCaseTypeUserRoleValidationError extends ValidationError {
    private EventACLEntity entity;

    public EventEntityCaseTypeUserRoleValidationError(final EventACLEntity entity) {
        super(String.format("UserRole '%s' is defined more than once for case type '%s'",
            entity.getUserRoleId(), entity.getEvent().getCaseType().getReference()));
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
