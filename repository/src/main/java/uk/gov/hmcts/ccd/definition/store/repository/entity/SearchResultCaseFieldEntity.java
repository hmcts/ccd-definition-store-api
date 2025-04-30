package uk.gov.hmcts.ccd.definition.store.repository.entity;

import uk.gov.hmcts.ccd.definition.store.repository.LayoutSheetType;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Table(name = "search_result_case_field")
@Entity
public class SearchResultCaseFieldEntity extends GenericLayoutEntity {

    @Embedded
    private SortOrder sortOrder;

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String getSheetName() {
        return "SearchResultFields";
    }

    @Override
    public LayoutSheetType getLayoutSheetType() {
        return LayoutSheetType.RESULT;
    }
}
