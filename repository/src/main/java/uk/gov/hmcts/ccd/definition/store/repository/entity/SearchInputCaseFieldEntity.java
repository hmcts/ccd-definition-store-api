package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;

@Table(name = "search_input_case_field")
@Entity
public class SearchInputCaseFieldEntity extends GenericLayoutEntity {

    @Column(name = "show_condition")
    private String showCondition;

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }
}
