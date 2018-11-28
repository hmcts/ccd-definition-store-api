package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "search_alias_field")
@Entity
public class SearchAliasFieldEntity implements Serializable {

    private static final long serialVersionUID = 5750963305463902612L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @OneToOne(fetch = EAGER)
    @JoinColumn(name = "case_field_id", nullable = false)
    private CaseFieldEntity caseField;

    @Column(name = "mapper_field_type", nullable = false)
    private String mapperFieldType;

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public String getMapperFieldType() {
        return mapperFieldType;
    }

    public void setMapperFieldType(String mapperFieldType) {
        this.mapperFieldType = mapperFieldType;
    }
}
