package uk.gov.hmcts.ccd.definition.store.domain.validation.category;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

@Component
public class CategoryEntitySomethingValidatorImpl implements CategoryEntityValidator {

    @Override
    public ValidationResult validate(final CategoryEntity categoryEntity,
                                     final CategoryEntityValidationContext categoryEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

//        for (StateACLEntity entity : stateEntity.getStateACLEntities()) {
//            if (!isValidCrud(entity.getCrudAsString())) {
//                String message = String.format("Invalid CRUD value '%s' for case type '%s', state '%s'",
//                    defaultString(entity.getCrudAsString()),
//                    stateEntityValidationContext.getCaseReference(),
//                    entity.getStateEntity().getReference());
//                validationResult.addError(new CategoryEntitySomethingValidatorImpl.ValidationError(message, entity));
//            }
//        }

        return validationResult;
    }

//    public static class ValidationError extends SimpleValidationError<StateACLEntity> {
//
//        public ValidationError(String defaultMessage, StateACLEntity entity) {
//            super(defaultMessage, entity);
//        }
//
//        @Override
//        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
//            return validationErrorMessageCreator.createErrorMessage(this);
//        }
//    }

}
