package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

@Component
public class EventEntityACLValidatorImpl implements EventEntityValidator {

    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (EventACLEntity entity : event.getEventACLEntities()) {

            if (null == entity.getUserRole()) {
                validationResult.addError(new EventEntityInvalidUserRoleValidationError(entity,
                    new AuthorisationEventValidationContext(event, eventEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
