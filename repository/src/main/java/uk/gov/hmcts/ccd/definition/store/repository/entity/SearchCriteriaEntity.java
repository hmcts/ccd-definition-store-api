package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;

import static jakarta.persistence.FetchType.LAZY;

@Table(name = "search_criteria")
@Entity
public class SearchCriteriaEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "live_from")
    private Date liveFrom;

    @Column(name = "live_to")
    private Date liveTo;

    @Column(name = "other_case_reference", nullable = false)
    private String otherCaseReference;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    public Date getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(Date liveTo) {
        this.liveTo = liveTo;
    }

    public String getOtherCaseReference() {
        return otherCaseReference;
    }

    public void setOtherCaseReference(String otherCaseReference) {
        this.otherCaseReference = otherCaseReference;
    }
}
