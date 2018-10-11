package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class StateEntityValidationContext implements ValidationContext {

    private final String caseName;

    private final String caseReference;

    public StateEntityValidationContext(CaseTypeEntity parentCaseType) {
        this.caseName = parentCaseType.getName();
        this.caseReference = parentCaseType.getReference();
    }

    public String getCaseName() {
        return this.caseName;
    }

    public String getCaseReference() {
        return this.caseReference;
    }

}
