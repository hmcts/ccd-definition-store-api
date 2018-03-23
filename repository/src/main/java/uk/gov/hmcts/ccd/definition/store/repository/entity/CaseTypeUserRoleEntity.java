package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name = "case_type_user_role")
@Entity
public class CaseTypeUserRoleEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable=false)
    private CaseTypeEntity caseType;

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(@NotNull final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

}
