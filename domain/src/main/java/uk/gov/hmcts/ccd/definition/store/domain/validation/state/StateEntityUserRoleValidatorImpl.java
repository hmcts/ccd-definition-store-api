package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateUserRoleEntity;

@Component
public class StateEntityUserRoleValidatorImpl implements StateEntityValidator {

    @Override
    public ValidationResult validate(final StateEntity stateEntity,
                                     final StateEntityValidationContext stateEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (StateUserRoleEntity entity : stateEntity.getStateUserRoles()) {
            if (null == entity.getUserRole()) {
                String message = String.format("Invalid UserRole for case type '%s', case state '%s'",
                    stateEntityValidationContext.getCaseReference(),
                    entity.getStateEntity().getReference());
                validationResult.addError(new ValidationError(message, entity));
            }
        }
        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<StateUserRoleEntity> {

        public ValidationError(String defaultMessage, StateUserRoleEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
