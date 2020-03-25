package uk.gov.hmcts.ccd.definition.store.domain.validation.category;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CategoryEntityValidationContext implements ValidationContext {

    private final String caseName;

    private final String caseReference;

    public CategoryEntityValidationContext(CaseTypeEntity parentCaseType) {
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
