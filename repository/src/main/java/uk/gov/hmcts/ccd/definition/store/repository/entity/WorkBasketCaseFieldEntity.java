package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "workbasket_case_field")
@Entity
public class WorkBasketCaseFieldEntity extends GenericLayoutEntity {

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
        return "WorkBasketResultFields";
    }
}
