package uk.gov.hmcts.ccd.definition.store.repository.entity;

import uk.gov.hmcts.ccd.definition.store.repository.LayoutSheetType;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Table(name = "search_cases_result_fields")
@Entity
public class SearchCasesResultFieldEntity extends GenericLayoutEntity {

    @Embedded
    private SortOrder sortOrder;

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Column(name = "use_case")
    private String useCase;

    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    @Override
    public String getSheetName() {
        return "SearchCasesResultFields";
    }

    @Override
    public LayoutSheetType getLayoutSheetType() {
        return LayoutSheetType.RESULT;
    }
}
