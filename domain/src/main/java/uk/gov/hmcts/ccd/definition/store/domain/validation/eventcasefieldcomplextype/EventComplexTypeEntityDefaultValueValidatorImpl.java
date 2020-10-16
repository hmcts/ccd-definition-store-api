package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GlobalCaseRole;

import java.util.regex.Pattern;

@Component
public class EventComplexTypeEntityDefaultValueValidatorImpl implements EventComplexTypeEntityValidator {

    public static final String ORGANISATION_POLICY_ROLE = "OrgPolicyCaseAssignedRole";

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        if (isOrgPolicyCaseAssignedRole(eventCaseFieldEntity.getReference())
            && !eventCaseFieldEntityValidationContext.getCaseRoles().contains(eventCaseFieldEntity.getDefaultValue())
            && !GlobalCaseRole.all().contains(eventCaseFieldEntity.getDefaultValue())) {
            validationResult.addError(
                new EventComplexTypeEntityDefaultValueError(
                    eventCaseFieldEntity,
                    eventCaseFieldEntityValidationContext
                ));
        }
        return validationResult;
    }

    private boolean isOrgPolicyCaseAssignedRole(String reference) {
        String[] referenceArray = reference.split(Pattern.quote("."));
        return ORGANISATION_POLICY_ROLE.equals(referenceArray[referenceArray.length - 1]);
    }
}
