package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

@Component
public class CaseTypeEntityCaseRoleValidatorImpl implements CaseTypeEntityValidator {

    private List<CaseRoleEntityValidator> caseRoleEntityValidators;

    @Autowired
    public CaseTypeEntityCaseRoleValidatorImpl(List<CaseRoleEntityValidator> caseRoleEntityValidators) {
        this.caseRoleEntityValidators = caseRoleEntityValidators;
    }

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {

        ValidationResult validationResult = new ValidationResult();

        for (CaseRoleEntityValidator caseRoleEntityValidator : caseRoleEntityValidators) {
            for (CaseRoleEntity caseRoleEntity : caseType.getCaseRoles()) {
                validationResult.merge(caseRoleEntityValidator.validate(
                    caseRoleEntity,
                    new CaseRoleEntityValidationContext(caseType)
                    )
                );
            }
        }
        return validationResult;
    }
}
