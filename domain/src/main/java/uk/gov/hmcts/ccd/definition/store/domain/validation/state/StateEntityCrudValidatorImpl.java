package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.CrudValidator.isValidCrud;

@Component
public class StateEntityCrudValidatorImpl implements StateEntityValidator {

    @Override
    public ValidationResult validate(final StateEntity stateEntity,
                                     final StateEntityValidationContext stateEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        for (StateACLEntity entity : stateEntity.getStateACLEntities()) {
            if (!isValidCrud(entity.getCrudAsString())) {
                String message = String.format("Invalid CRUD value '%s' for case type '%s', state '%s'",
                    defaultString(entity.getCrudAsString()),
                    stateEntityValidationContext.getCaseReference(),
                    entity.getStateEntity().getReference());
                validationResult.addError(new StateEntityCrudValidatorImpl.ValidationError(message, entity));
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
