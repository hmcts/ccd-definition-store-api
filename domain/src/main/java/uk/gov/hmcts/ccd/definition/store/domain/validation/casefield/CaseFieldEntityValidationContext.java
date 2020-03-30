package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CaseFieldEntityValidationContext implements ValidationContext {

    private final SecurityClassification parentSecurityClassification;
    private final String caseName;
    private final String caseReference;
    private final List<String> categories;

    public CaseFieldEntityValidationContext(CaseTypeEntity parentCaseType) {
        this.caseName = parentCaseType.getName();
        this.caseReference = parentCaseType.getReference();
        this.parentSecurityClassification = parentCaseType.getSecurityClassification();
        this.categories = parentCaseType.getCategories().stream().map(CategoryEntity::getCategoryId)
            .collect(Collectors.toList());
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

    public List<String> getCategories() {
        return categories;
    }
}
