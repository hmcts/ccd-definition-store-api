package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

@Component
public class EventEntitySecurityClassificationValidatorImpl implements EventEntityValidator {

    @Override
    public ValidationResult validate(EventEntity event,
                                     EventEntityValidationContext validationContext) {

        ValidationResult validationResult = new ValidationResult();

        if (event.getSecurityClassification() == null) {
            validationResult.addError(
                new EventEntityMissingSecurityClassificationValidationError(event)
            );
            return validationResult;
        }

        SecurityClassification parentSecurityClassification = validationContext.getParentSecurityClassification();

        if (parentSecurityClassification != null
            && parentSecurityClassification.isMoreRestrictiveThan(event.getSecurityClassification())) {
            validationResult.addError(
                new EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    event, validationContext)
            );
        }

        return validationResult;

    }
}
