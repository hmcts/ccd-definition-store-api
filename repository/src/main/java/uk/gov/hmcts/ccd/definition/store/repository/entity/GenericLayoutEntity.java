package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@MappedSuperclass
public abstract class GenericLayoutEntity implements DefEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "case_field_id", nullable = false)
    private CaseFieldEntity caseField;

    @Column(name = "label")
    private String label;

    @Column(name = "display_order")
    private Integer order;

    public Integer getId() {
        return id;
    }

    public LocalDate getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(LocalDate liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDate getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(LocalDate liveTo) {
        this.liveTo = liveTo;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(final CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }
}
