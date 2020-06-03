package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.List;

@Component
public class EventComplexTypeEntityDefaultValueValidatorImpl implements EventComplexTypeEntityValidator {

    private static final String GLOBAL_ROLE_CREATOR = "[CREATOR]";
    private static final String GLOBAL_ROLE_COLLABORATOR = "[COLLABORATOR]";

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity, EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        if (eventCaseFieldEntity.getReference().equals("OrgPolicyCaseAssignedRole")) {
            if (!getAllRolesForValidation(eventCaseFieldEntityValidationContext.getCaseRoles()).contains(eventCaseFieldEntity.getDefaultValue())) {
                validationResult.addError(
                        new EventComplexTypeEntityDefaultValueError(
                                eventCaseFieldEntity,
                                eventCaseFieldEntityValidationContext
                        ));

            }
        }
        return validationResult;
    }

    private List<String> getAllRolesForValidation(List<String> caseRoles) {
        caseRoles.add(GLOBAL_ROLE_CREATOR);
        caseRoles.add(GLOBAL_ROLE_COLLABORATOR);
        return caseRoles;
    }
}
