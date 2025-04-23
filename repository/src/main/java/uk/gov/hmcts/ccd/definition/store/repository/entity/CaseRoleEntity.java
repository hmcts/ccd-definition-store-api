package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@DiscriminatorValue("CASEROLE")
public class CaseRoleEntity extends AccessProfileEntity implements Serializable {
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
