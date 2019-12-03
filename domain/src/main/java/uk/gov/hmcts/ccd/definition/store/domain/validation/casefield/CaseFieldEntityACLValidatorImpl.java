package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import static uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleServiceImpl.isCaseRole;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CaseFieldEntityACLValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();
        Set<String> caseRoles = caseFieldEntityValidationContext.getCaseTypeCaseRoles();

        for (CaseFieldACLEntity entity : caseField.getCaseFieldACLEntities()) {

            if (null == entity.getUserRole()) {
                validationResult.addError(new CaseFieldEntityInvalidUserRoleValidationError(entity,
                    new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            } else if (isCaseRole(entity.getUserRole().getReference()) && !caseRoles.contains(entity.getUserRole().getReference())) {
                validationResult.addError(new CaseFieldEntityInvalidCaseRoleValidationError(entity,
                    new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
