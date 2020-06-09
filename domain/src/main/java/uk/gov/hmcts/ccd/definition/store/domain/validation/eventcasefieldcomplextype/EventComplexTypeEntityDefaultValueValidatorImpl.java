package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GlobalCaseRole;

@Component
public class EventComplexTypeEntityDefaultValueValidatorImpl implements EventComplexTypeEntityValidator {

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity, EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        if (eventCaseFieldEntity.getReference().equals("OrgPolicyCaseAssignedRole")) {
            if (!eventCaseFieldEntityValidationContext.getCaseRoles().contains(eventCaseFieldEntity.getDefaultValue()) &&
                    !GlobalCaseRole.all().contains(eventCaseFieldEntity.getDefaultValue())

            ) {
                validationResult.addError(
                        new EventComplexTypeEntityDefaultValueError(
                                eventCaseFieldEntity,
                                eventCaseFieldEntityValidationContext
                        ));

            }
        }
        return validationResult;
    }
}
