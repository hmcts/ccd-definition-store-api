package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;

@Table(name = "search_input_case_field")
@Entity
public class SearchInputCaseFieldEntity extends InputCaseFieldEntity {

    @Override
    public String getSheetName() {
        return "SearchInputFields";
    }
}
