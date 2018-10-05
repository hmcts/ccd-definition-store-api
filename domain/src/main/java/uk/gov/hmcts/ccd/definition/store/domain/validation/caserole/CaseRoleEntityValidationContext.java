package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

public class CaseRoleEntityValidationContext implements ValidationContext {
    private final String caseName;
    private final String caseReference;
    private final List<CaseRoleEntity> caseRoleEntities;

    public CaseRoleEntityValidationContext(CaseTypeEntity parentCaseType) {
        this.caseName = parentCaseType.getName();
        this.caseReference = parentCaseType.getReference();
        this.caseRoleEntities = parentCaseType.getCaseRoles();
    }

    public String getCaseName() {
        return this.caseName;
    }

    public String getCaseReference() {
        return this.caseReference;
    }

    public List<CaseRoleEntity> getCaseRoleEntities() {
        return caseRoleEntities;
    }
}
