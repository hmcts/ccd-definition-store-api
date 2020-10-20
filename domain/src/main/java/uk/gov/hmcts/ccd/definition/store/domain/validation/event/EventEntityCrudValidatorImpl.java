package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.CrudValidator.isValidCrud;

@Component
public class EventEntityCrudValidatorImpl implements EventEntityValidator {

    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        for (EventACLEntity entity : event.getEventACLEntities()) {
            if (!isValidCrud(entity.getCrudAsString())) {
                validationResult.addError(new EventEntityInvalidCrudValidationError(entity,
                    new AuthorisationEventValidationContext(event, eventEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
