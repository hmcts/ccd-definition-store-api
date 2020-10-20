package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "case_field_acl")
@Entity
public class CaseFieldACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_field_id", nullable = false)
    private CaseFieldEntity caseField;

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(final CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public CaseTypeEntity getCaseType() {
        return getCaseField().getCaseType();
    }
}
