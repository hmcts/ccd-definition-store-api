package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;

public interface CaseRoleEntityValidator {
    ValidationResult validate(CaseRoleEntity caseRoleEntity,
                              CaseRoleEntityValidationContext caseRoleEntityValidationContext);
}
