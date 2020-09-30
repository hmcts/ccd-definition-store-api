package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.List;

@Component
public class EventEntityEventCaseFieldsValidatorImpl implements EventEntityValidator {

    final List<EventCaseFieldEntityValidator> eventCaseFieldValidators;

    public EventEntityEventCaseFieldsValidatorImpl(List<EventCaseFieldEntityValidator> eventCaseFieldValidators) {
        this.eventCaseFieldValidators = eventCaseFieldValidators;
    }

    @Override
    public ValidationResult validate(EventEntity caseEvent, EventEntityValidationContext eventEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();
        List<EventCaseFieldEntity> allEventCaseFieldsForEvent = caseEvent.getEventCaseFields();

        for (EventCaseFieldEntity eventCaseField : allEventCaseFieldsForEvent) {
            for (EventCaseFieldEntityValidator eventCaseFieldValidator : eventCaseFieldValidators) {
                validationResult.merge(eventCaseFieldValidator.validate(
                    eventCaseField,
                    new EventCaseFieldEntityValidationContext(caseEvent.getReference(),
                        allEventCaseFieldsForEvent,
                        eventEntityValidationContext.getCaseRoles())
                    )
                );
            }
        }

        return validationResult;
    }
}
