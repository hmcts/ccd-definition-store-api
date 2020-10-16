package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_CASE_PAYMENT_HISTORY_VIEWER;

@Component
public class EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator implements EventCaseFieldEntityValidator {

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();

        if (isCasePaymentHistoryViewer(eventCaseFieldEntity)
            && !isEmptyDisplayContext(eventCaseFieldEntity)
            && !isReadOnlyDisplayContext(eventCaseFieldEntity)) {
            validationResult.addError(
                new EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator.ValidationError(
                    String.format(
                        "'%s' is CasePaymentHistoryViewer type and cannot be editable for event with reference '%s'",
                        eventCaseFieldEntity.getCaseField() != null
                            ? eventCaseFieldEntity.getCaseField().getReference() : "",
                        eventCaseFieldEntity.getEvent() != null ? eventCaseFieldEntity.getEvent().getReference() : ""),
                    eventCaseFieldEntity)
            );
        }

        return validationResult;
    }


    private boolean isCasePaymentHistoryViewer(EventCaseFieldEntity eventCaseFieldEntity) {
        return BASE_CASE_PAYMENT_HISTORY_VIEWER.equals(
            eventCaseFieldEntity.getCaseField().getFieldType().getReference());
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
