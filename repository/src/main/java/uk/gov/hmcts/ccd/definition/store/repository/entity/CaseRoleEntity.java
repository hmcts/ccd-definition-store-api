package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static javax.persistence.FetchType.LAZY;

@Table(name = "role")
@Entity
@DiscriminatorValue("CASEROLE")
public class CaseRoleEntity extends UserRoleEntity implements Serializable {
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id")
    private CaseTypeEntity caseType;

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

}
