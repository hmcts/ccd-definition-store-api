package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "noc_config")
@Entity
public class NoCConfigEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "reasons_required")
    private boolean reasonsRequired;

    @Column(name = "noc_action_interpretation_required")
    private boolean nocActionInterpretationRequired;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isReasonsRequired() {
        return reasonsRequired;
    }

    public void setReasonsRequired(boolean reasonsRequired) {
        this.reasonsRequired = reasonsRequired;
    }

    public boolean isNocActionInterpretationRequired() {
        return nocActionInterpretationRequired;
    }

    public void setNocActionInterpretationRequired(boolean nocActionInterpretationRequired) {
        this.nocActionInterpretationRequired = nocActionInterpretationRequired;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public void copy(NoCConfigEntity configEntity) {
        this.setNocActionInterpretationRequired(configEntity.isNocActionInterpretationRequired());
        this.setReasonsRequired(configEntity.isReasonsRequired());
    }

    @Override
    public String toString() {
        return "NoCConfigEntity{"
            + "id=" + id
            + ", createdAt=" + createdAt
            + ", reasonsRequired='" + reasonsRequired + '\''
            + ", nocActionInterpretationRequired='" + nocActionInterpretationRequired + '\''
            + ", caseType='" + caseType + '\''
            + '}';
    }
}
