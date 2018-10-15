package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name = "case_type_user_role")
@Entity
public class CaseTypeLiteACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeLiteEntity caseType;

    public CaseTypeLiteEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(@NotNull final CaseTypeLiteEntity caseType) {
        this.caseType = caseType;
    }

}
