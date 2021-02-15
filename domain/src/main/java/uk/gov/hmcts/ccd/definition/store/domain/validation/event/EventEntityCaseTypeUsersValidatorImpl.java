package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.List;


@Component
public class EventEntityCaseTypeUsersValidatorImpl implements EventEntityValidator {

    private final List<String> caseTypesContainingCaseworkerCaa = new ArrayList<>();
    private final List<String> caseTypesContainingCaseworkerApprover = new ArrayList<>();

    @Override
    public ValidationResult validate(final EventEntity event,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (EventACLEntity entity : event.getEventACLEntities()) {

            switch (entity.getUserRoleId().toLowerCase()) {
                case "caseworker-caa":
                    if (checkIfDuplicate(entity.getEvent().getCaseType().getReference(),
                        caseTypesContainingCaseworkerCaa)) {
                        validationResult.addError(new EventEntityCaseTypeUsersValidationError(entity));
                    } else {
                        caseTypesContainingCaseworkerCaa.add(entity.getEvent().getCaseType().getReference());
                    }
                    break;
                case "caseworker-approver":
                    if (checkIfDuplicate(entity.getEvent().getCaseType().getReference(),
                        caseTypesContainingCaseworkerApprover)) {
                        validationResult.addError(new EventEntityCaseTypeUsersValidationError(entity));
                    } else {
                        caseTypesContainingCaseworkerApprover.add(entity.getEvent().getCaseType().getReference());
                    }
                    break;
                default:
                    break;
            }
        }
        return validationResult;
    }

    private boolean checkIfDuplicate(String caseType, List<String> list) {
        for (String i : list) {
            if (i.equalsIgnoreCase(caseType)) {
                return true;
            }
        }
        return false;
    }
}
