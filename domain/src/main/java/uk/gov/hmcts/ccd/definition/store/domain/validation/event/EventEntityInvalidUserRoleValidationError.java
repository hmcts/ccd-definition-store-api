package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;

public class EventEntityInvalidUserRoleValidationError extends ValidationError {

    private final EventACLEntity eventACLEntity;
    private final AuthorisationEventValidationContext authorisationEventValidationContext;

    public EventEntityInvalidUserRoleValidationError(final EventACLEntity entity,
                                                     final AuthorisationEventValidationContext context) {
        super(String.format("Invalid UserRole %s for case type '%s', event '%s'",
            entity.getUserRoleId(),
            context.getCaseReference(),
            context.getEventReference()));
        this.eventACLEntity = entity;
        this.authorisationEventValidationContext = context;
    }

    public EventACLEntity getEventACLEntity() {
        return eventACLEntity;
    }

    public AuthorisationEventValidationContext getAuthorisationEventValidationContext() {
        return authorisationEventValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
