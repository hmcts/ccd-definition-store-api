package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;

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

}
