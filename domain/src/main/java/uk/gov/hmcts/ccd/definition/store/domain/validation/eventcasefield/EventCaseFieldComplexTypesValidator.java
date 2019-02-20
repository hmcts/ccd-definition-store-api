package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.List;

@Component
public class EventCaseFieldComplexTypesValidator implements EventCaseFieldEntityValidator {

    private final List<EventComplexTypeEntityValidator> eventComplexTypeEntityValidators;

    public EventCaseFieldComplexTypesValidator(List<EventComplexTypeEntityValidator> eventComplexTypeEntityValidators) {
        this.eventComplexTypeEntityValidators = eventComplexTypeEntityValidators;
    }

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();
        List<EventComplexTypeEntity> eventComplexTypes = eventCaseFieldEntity.getEventComplexTypes();

        for (EventComplexTypeEntity eventComplexType : eventComplexTypes) {
            for (EventComplexTypeEntityValidator eventComplexTypeEntityValidator : eventComplexTypeEntityValidators) {
                validationResult.merge(eventComplexTypeEntityValidator
                    .validate(eventComplexType, eventCaseFieldEntityValidationContext));
            }
        }

        return validationResult;
    }
}
