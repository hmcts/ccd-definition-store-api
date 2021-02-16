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
        initUserRoleCaseTypeMap();

        for (EventACLEntity entity : event.getEventACLEntities()) {

            String userRole = entity.getUserRoleId().toLowerCase();
            String caseType = entity.getEvent().getCaseType().getReference().toLowerCase();

            if (userRoleCaseTypeMap.containsKey(userRole)) {
                if (userRoleCaseTypeMap.get(userRole).contains(caseType)) {
                    validationResult.addError(new EventEntityCaseTypeUserRoleValidationError(entity));
                } else {
                    userRoleCaseTypeMap.get(userRole).add(caseType);
                }
            }
        }
        return validationResult;
    }

    private void initUserRoleCaseTypeMap() {
        for (String userRole : userRoleList) {
            if (!userRoleCaseTypeMap.containsKey(userRole)) {
                userRoleCaseTypeMap.put(userRole, new ArrayList<>());
            }
        }
    }
}
