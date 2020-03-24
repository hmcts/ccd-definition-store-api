package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "workbasket_input_case_field")
@Entity
public class WorkBasketInputCaseFieldEntity extends GenericLayoutEntity {

    @Column(name = "show_condition")
    private String showCondition;

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

}
