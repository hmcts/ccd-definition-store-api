package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EventCaseFieldDisplayContextValidatorImpl implements EventCaseFieldEntityValidator {

    private final List<FieldTypeToDisplayContextValidator> fieldTypeToDisplayContextValidators;

    @Autowired
    public EventCaseFieldDisplayContextValidatorImpl(List<FieldTypeToDisplayContextValidator> fieldTypeToDisplayContextValidators) {
        this.fieldTypeToDisplayContextValidators = fieldTypeToDisplayContextValidators;
    }

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();
        DisplayContext displayContext = eventCaseFieldEntity.getDisplayContext();
        if (displayContext == null) {
            validationResult.addError(new ValidationError("Couldn't find the column DisplayContext or " +
                "incorrect value specified for DisplayContext. Allowed values are 'READONLY','MANDATORY' or 'OPTIONAL'",
                eventCaseFieldEntity));
        }
        validationResult.addErrors(validateAndGetErrors(eventCaseFieldEntity));
        return validationResult;
    }

    private List<uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError> validateAndGetErrors(EventCaseFieldEntity eventCaseFieldEntity) {
        return fieldTypeToDisplayContextValidators.stream()
            .map(v -> v.validate(eventCaseFieldEntity))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
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
