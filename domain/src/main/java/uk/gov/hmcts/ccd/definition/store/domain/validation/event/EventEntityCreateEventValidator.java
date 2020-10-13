package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

@Component
public class EventEntityCreateEventValidator implements EventEntityValidator {

    @Override
    public ValidationResult validate(EventEntity caseEvent, EventEntityValidationContext eventEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();

        if (caseEvent.isCanCreate() && caseEvent.getPostStates().size() == 0) {
            validationResult.addError(
                new CreateEventDoesNotHavePostStateValidationError(caseEvent)
            );
        }

        return validationResult;
    }

}
