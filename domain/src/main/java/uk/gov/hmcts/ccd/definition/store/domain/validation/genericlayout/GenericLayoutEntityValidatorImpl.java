package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

@Component
public class GenericLayoutEntityValidatorImpl implements GenericLayoutValidator {

    public static final String ERROR_MESSAGE_INVALID_NUMBER_WITH_CASE_FIELD =
        "DisplayOrder '%d' needs to be a valid integer for row with label '%s', case field '%s'";
    public static final String ERROR_MESSAGE_INVALID_NUMBER_WITHOUT_CASE_FIELD =
        "DisplayOrder '%d' needs to be a valid integer for row with label '%s'";

    @Override
    public ValidationResult validate(GenericLayoutEntity entity) {
        final ValidationResult validationResult = new ValidationResult();
        if (entity.getCaseType() == null) {
            validationResult.addError(
                new ValidationError(
                    String.format("Case Type cannot be empty for row with label '%s', case field '%s'",
                        entity.getLabel(),
                        (entity.getCaseField() != null ? entity.getCaseField().getReference() : "")
                    ), entity)
            );
        }
        if (entity.getCaseField() == null) {
            validationResult.addError(
                new ValidationError(
                    String.format("Case Field cannot be empty for row with label '%s', case type '%s'",
                        entity.getLabel(),
                        (entity.getCaseType() != null ? entity.getCaseType().getReference() : "")
                    ), entity)
            );
        }
        if (entity.getOrder() != null && entity.getOrder() < 1) {
            final String errorMessage;
            if (null == entity.getCaseField()) {
                errorMessage =
                    String.format(ERROR_MESSAGE_INVALID_NUMBER_WITHOUT_CASE_FIELD,
                                  entity.getOrder(),
                                  entity.getLabel());
            } else {
                errorMessage =
                    String.format(ERROR_MESSAGE_INVALID_NUMBER_WITH_CASE_FIELD,
                                  entity.getOrder(),
                                  entity.getLabel(),
                                  entity.getCaseField().getReference());
            }
            validationResult.addError(new ValidationError(errorMessage, entity));
        }
        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<GenericLayoutEntity> {

        public ValidationError(String defaultMessage, GenericLayoutEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
