package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldOrderSummaryCaseFieldValidator implements EventCaseFieldEntityValidator {

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();

        if (isOrderSummaryType(eventCaseFieldEntity)
            && !isEmptyDisplayContext(eventCaseFieldEntity)
            && !isMandatoryDisplayContext(eventCaseFieldEntity)) {
            validationResult.addError(
                new EventCaseFieldOrderSummaryCaseFieldValidator.ValidationError(
                    String.format("'%s' is OrderSummary type and has to be mandatory "
                            + "(not editable but has to be added to a form in UI) for event with reference '%s'",
                        eventCaseFieldEntity.getCaseField() != null
                            ? eventCaseFieldEntity.getCaseField().getReference() : "",
                        eventCaseFieldEntity.getEvent() != null ? eventCaseFieldEntity.getEvent().getReference() : ""
                    ),
                    eventCaseFieldEntity)
            );
        }

        return validationResult;
    }

    private boolean isOrderSummaryType(EventCaseFieldEntity eventCaseFieldEntity) {
        return "OrderSummary".equals(eventCaseFieldEntity.getCaseField().getFieldType().getReference());
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
