package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

@Component
public class StateEntityACLValidatorImpl implements StateEntityValidator {

    @Override
    public ValidationResult validate(final StateEntity stateEntity,
                                     final StateEntityValidationContext stateEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (StateACLEntity entity : stateEntity.getStateACLEntities()) {
            if (null == entity.getUserRole()) {
                String message = String.format("Invalid UserRole %s for case type '%s', case state '%s'",
                    entity.getUserRoleId(),
                    stateEntityValidationContext.getCaseReference(),
                    entity.getStateEntity().getReference());
                validationResult.addError(new ValidationError(message, entity));
            }
        }
        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<StateACLEntity> {

        public ValidationError(String defaultMessage, StateACLEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
