package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name = "case_type_acl")
@Entity
public class CaseTypeACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(@NotNull final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

}
