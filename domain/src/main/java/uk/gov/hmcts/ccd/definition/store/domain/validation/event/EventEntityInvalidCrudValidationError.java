package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class EventEntityInvalidCrudValidationError extends ValidationError {

    private final AuthorisationEventValidationContext authorisationEventValidationContext;
    private final EventACLEntity eventACLEntity;

    public EventEntityInvalidCrudValidationError(final EventACLEntity entity,
                                                 final AuthorisationEventValidationContext context) {
        super(String.format("Invalid CRUD value '%s' for case type '%s', event '%s'",
            defaultString(entity.getCrudAsString()),
            context.getCaseReference(),
            context.getEventReference()));

        this.eventACLEntity = entity;
        this.authorisationEventValidationContext = context;
    }

    public AuthorisationEventValidationContext getAuthorisationEventValidationContext() {
        return authorisationEventValidationContext;
    }

    public EventACLEntity getEventACLEntity() {
        return eventACLEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
