package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CaseFieldEntityValidationContext implements ValidationContext {

    private final SecurityClassification parentSecurityClassification;
    private final String caseName;
    private final String caseReference;
    private final List<CaseRoleEntity> caseRoles;

    public CaseFieldEntityValidationContext(CaseTypeEntity parentCaseType) {
        this.caseName = parentCaseType.getName();
        this.caseReference = parentCaseType.getReference();
        this.parentSecurityClassification = parentCaseType.getSecurityClassification();
        this.caseRoles = parentCaseType.getCaseRoles();
    }

    public SecurityClassification getParentSecurityClassification() {
        return this.parentSecurityClassification;
    }

    public String getCaseName() {
        return this.caseName;
    }

    public String getCaseReference() {
        return this.caseReference;
    }

    public Set<String> getCaseTypeCaseRoles() {
        return caseRoles.stream().map(CaseRoleEntity::getReference).collect(Collectors.toSet());
    }
}
