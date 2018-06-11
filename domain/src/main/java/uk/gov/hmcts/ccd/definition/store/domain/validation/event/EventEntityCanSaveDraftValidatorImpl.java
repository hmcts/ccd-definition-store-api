package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

@Component
public class EventEntityCanSaveDraftValidatorImpl implements EventEntityValidator {
    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        if(!event.getPreStates().isEmpty() && event.getCanSaveDraft() == Boolean.TRUE) {
            validationResult.addError(new ValidationError(String.format("Enable saving draft is only available for " +
                "Create events. Event %s is not eligible.", event.getName()),
                event));
        }

        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<EventEntity> {
        public ValidationError(String defaultMessage, EventEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
