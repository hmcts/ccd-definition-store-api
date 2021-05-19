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
public class EventEntityCaseTypeAccessProfileValidatorImpl implements EventEntityValidator {

    private final Map<String, ArrayList<String>> accessProfileCaseTypeMap = new HashMap<>();
    private final List<String> accessProfilesLimitedToOneEventPerCaseType
            = Arrays.asList("caseworker-caa", "caseworker-approver");

    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();
        initAccessProfileCaseTypeMap();

        for (EventACLEntity entity : event.getEventACLEntities()) {

            String accessProfileId = entity.getAccessProfileId().toLowerCase();
            String caseType = entity.getEvent().getCaseType().getReference().toLowerCase();

            if (accessProfileCaseTypeMap.containsKey(accessProfileId)) {
                if (accessProfileCaseTypeMap.get(accessProfileId).contains(caseType)) {
                    validationResult.addError(new EventEntityCaseTypeAccessProfileValidationError(entity));
                } else {
                    accessProfileCaseTypeMap.get(accessProfileId).add(caseType);
                }
            }
        }
        return validationResult;
    }

    private void initAccessProfileCaseTypeMap() {
        for (String accessProfile : accessProfilesLimitedToOneEventPerCaseType) {
            if (!accessProfileCaseTypeMap.containsKey(accessProfile)) {
                accessProfileCaseTypeMap.put(accessProfile, new ArrayList<>());
            }
        }
    }
}
