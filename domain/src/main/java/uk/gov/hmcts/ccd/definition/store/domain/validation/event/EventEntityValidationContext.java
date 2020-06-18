package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.stream.Collectors;

public class EventEntityValidationContext implements ValidationContext {

    private final SecurityClassification parentSecurityClassification;

    private final String caseName;

    private final String caseReference;

    private final List<String> caseRoles;

    public EventEntityValidationContext(CaseTypeEntity parentCaseType) {
        this.caseName = parentCaseType.getName();
        this.caseReference = parentCaseType.getReference();
        this.parentSecurityClassification = parentCaseType.getSecurityClassification();
        this.caseRoles = parentCaseType.getCaseRoles().stream().map(caseRole -> caseRole.getReference()).collect(Collectors.toList());
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

    public List<String> getCaseRoles() {
        return this.caseRoles;
    }
}
