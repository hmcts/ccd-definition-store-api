package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.Optional;

@Component
public class OrderSummaryFieldTypeToDisplayContextValidator implements FieldTypeToDisplayContextValidator {

    @Override
    public Optional<ValidationError> validate(EventCaseFieldEntity eventCaseFieldEntity) {
        if (isOrderSummaryType(eventCaseFieldEntity) && !isValid(eventCaseFieldEntity)) {
            return Optional.of(new EventCaseFieldDisplayContextValidatorImpl.ValidationError(
                "OrderSummary field type can only be configured with 'READONLY' DisplayContext",
                eventCaseFieldEntity));
        }
        return Optional.empty();
    }

    private boolean isOrderSummaryType(EventCaseFieldEntity eventCaseFieldEntity) {
        return FieldTypeUtils.isOrderSummary(eventCaseFieldEntity.getCaseField().getFieldType().getReference());
    }

    private boolean isValid(EventCaseFieldEntity eventCaseFieldEntity) {
        return eventCaseFieldEntity.getDisplayContext().equals(DisplayContext.READONLY);
    }
}
