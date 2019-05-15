package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "complex_field_acl")
@Entity
public class ComplexFieldACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_field_id", nullable = false)
    private CaseFieldEntity caseField;

    private String listElementCode;

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(final CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public String getListElementCode() {
        return listElementCode;
    }

    public void setListElementCode(String listElementCode) {
        this.listElementCode = listElementCode;
    }
}
