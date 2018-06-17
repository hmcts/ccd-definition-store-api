package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator implements EventCaseFieldEntityValidator {

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();

        if ("CasePaymentHistoryViewer".equals(eventCaseFieldEntity.getCaseField().getFieldType().getReference())
                && null != eventCaseFieldEntity.getDisplayContext()
                && !eventCaseFieldEntity.getDisplayContext().equals(DisplayContext.READONLY)) {
            validationResult.addError(
                new CasePaymentHistoryViewerTypeCannotBeEditableValidationError(eventCaseFieldEntity)
            );
        }

        return validationResult;
    }

}
