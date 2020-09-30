package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldMetadataValidatorImpl implements EventCaseFieldEntityValidator {

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();
        if (eventCaseFieldEntity.getCaseField().isMetadataField()
            && eventCaseFieldEntity.getDisplayContext() != DisplayContext.READONLY) {
            validationResult.addError(new ValidationError(
                String.format("'%s' is a metadata field and cannot be editable for event with reference '%s'",
                eventCaseFieldEntity.getCaseField() != null
                    ? eventCaseFieldEntity.getCaseField().getReference() : "",
                eventCaseFieldEntity.getEvent() != null
                    ? eventCaseFieldEntity.getEvent().getReference() : ""
            ),
                eventCaseFieldEntity));
        }
        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<EventCaseFieldEntity> {
        public ValidationError(String defaultMessage, EventCaseFieldEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
