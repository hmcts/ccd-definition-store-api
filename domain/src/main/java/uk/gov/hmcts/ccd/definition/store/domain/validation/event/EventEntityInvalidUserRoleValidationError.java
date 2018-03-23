package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventUserRoleEntity;

public class EventEntityInvalidUserRoleValidationError extends ValidationError {

    private final EventUserRoleEntity eventUserRoleEntity;
    private final AuthorisationEventValidationContext authorisationEventValidationContext;

    public EventEntityInvalidUserRoleValidationError(final EventUserRoleEntity entity,
                                                     final AuthorisationEventValidationContext context) {
        super(String.format("Invalid UserRole for case type '%s', event '%s'",
            context.getCaseReference(),
            context.getEventReference()));
        this.eventUserRoleEntity = entity;
        this.authorisationEventValidationContext = context;
    }

    public EventUserRoleEntity getEventUserRoleEntity() {
        return eventUserRoleEntity;
    }

    public AuthorisationEventValidationContext getAuthorisationEventValidationContext() {
        return authorisationEventValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
