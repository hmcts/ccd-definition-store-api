package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "workbasket_input_case_field")
@Entity
public class WorkBasketInputCaseFieldEntity extends InputCaseFieldEntity {

    @Override
    public String getSheetName() {
        return "WorkBasketInputFields";
    }
}
