package uk.gov.hmcts.ccd.definition.store.domain.validation.category;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;

public class CategoryEntityValidationContext implements ValidationContext {

    private final String caseName;
    private final String caseReference;
    private final List<CategoryEntity> categoryEntities;

    public CategoryEntityValidationContext(CaseTypeEntity parentCaseType) {
        this.caseName = parentCaseType.getName();
        this.caseReference = parentCaseType.getReference();
        this.categoryEntities = parentCaseType.getCategories();
    }

    public String getCaseName() {
        return this.caseName;
    }

    public String getCaseReference() {
        return this.caseReference;
    }

    public List<CategoryEntity> getCategoryEntities() {
        return categoryEntities;
    }
}
