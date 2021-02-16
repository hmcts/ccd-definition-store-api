package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Component
@RequestScope
public class EventEntityCaseTypeUserRoleValidatorImpl implements EventEntityValidator {

    private final Map<String, ArrayList<String>> caseTypeAndUserRole = new HashMap<>() {{
            put("caseworker-caa", new ArrayList<>());
            put("caseworker-approver", new ArrayList<>());
            }};

    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (EventACLEntity entity : event.getEventACLEntities()) {
            if (caseTypeAndUserRole.containsKey(entity.getUserRoleId().toLowerCase())) {
                if (caseTypeAndUserRole.get(entity.getUserRoleId().toLowerCase()).contains(entity.getEvent()
                    .getCaseType().getReference().toLowerCase())) {
                    validationResult.addError(new EventEntityCaseTypeUserRoleValidationError(entity));
                } else {
                    caseTypeAndUserRole.get(entity.getUserRoleId().toLowerCase()).add(entity.getEvent().getCaseType()
                        .getReference().toLowerCase());
                }
            }
        }
        return validationResult;
    }
}
