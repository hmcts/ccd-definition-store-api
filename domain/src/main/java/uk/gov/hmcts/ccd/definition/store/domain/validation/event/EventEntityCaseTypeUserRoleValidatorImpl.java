package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;


@Component
@RequestScope
public class EventEntityCaseTypeUserRoleValidatorImpl implements EventEntityValidator {

    private final Map<String, ArrayList<String>> userRoleCaseTypeMap = new HashMap<>();
    private final List<String> userRoleList = Arrays.asList("caseworker-caa", "caseworker-approver");

    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();
        mapUserRolesWithEmptyList(userRoleList);

        for (EventACLEntity entity : event.getEventACLEntities()) {
            if (userRoleCaseTypeMap.containsKey(entity.getUserRoleId().toLowerCase())) {
                if (userRoleCaseTypeMap.get(entity.getUserRoleId().toLowerCase()).contains(entity.getEvent()
                    .getCaseType().getReference().toLowerCase())) {
                    validationResult.addError(new EventEntityCaseTypeUserRoleValidationError(entity));
                } else {
                    userRoleCaseTypeMap.get(entity.getUserRoleId().toLowerCase()).add(entity.getEvent().getCaseType()
                        .getReference().toLowerCase());
                }
            }
        }
        return validationResult;
    }

    private void mapUserRolesWithEmptyList(List<String> list) {
        for (String userRole : list) {
            if (!(userRoleCaseTypeMap.containsKey(userRole))) {
                userRoleCaseTypeMap.put(userRole, new ArrayList<>());
            }
        }
    }
}
