package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

@Table(name = "case_field")
@Entity
@TypeDefs({
    @TypeDef(
        name = "pgsql_securityclassification_enum",
        typeClass = PostgreSQLEnumType.class,
        parameters = @Parameter(name = "type",
            value = "uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")
    ),
    @TypeDef(
        name = "pgsql_datafieldtype_enum",
        typeClass = PostgreSQLEnumType.class,
        parameters = @Parameter(name = "type",
            value = "uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType")
    )})
public class CaseFieldEntity implements FieldEntity, Serializable {

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

    @Column(name = "label")
    private String label;

    @Column(name = "hint")
    private String hint;

    @Column(name = "hidden")
    private Boolean hidden;

    @Column(name = "security_classification")
    @Type(type = "pgsql_securityclassification_enum")
    private SecurityClassification securityClassification;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "field_type_id", nullable = false)
    private FieldTypeEntity fieldType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_field_id")
    private final List<CaseFieldACLEntity> caseFieldACLEntities = new ArrayList<>();

    @Column(name = "data_field_type")
    @Type(type = "pgsql_datafieldtype_enum")
    private DataFieldType dataFieldType;

    public CaseFieldEntity() {
        this.dataFieldType = DataFieldType.CASE_DATA;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public LocalDate getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(final LocalDate liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDate getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(final LocalDate liveTo) {
        this.liveTo = liveTo;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(final String hint) {
        this.hint = hint;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(final Boolean hidden) {
        this.hidden = hidden;
    }

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(final SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    @Override
    public FieldTypeEntity getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldTypeEntity fieldType) {
        this.fieldType = fieldType;
    }

    public void setCaseType(final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public List<CaseFieldACLEntity> getCaseFieldACLEntities() {
        return caseFieldACLEntities;
    }

    public DataFieldType getDataFieldType() {
        return dataFieldType;
    }

    public void setDataFieldType(DataFieldType dataFieldType) {
        this.dataFieldType = dataFieldType;
    }

    public CaseFieldEntity addCaseFieldACL(final CaseFieldACLEntity caseFieldACLEntity) {
        caseFieldACLEntity.setCaseField(this);
        caseFieldACLEntities.add(caseFieldACLEntity);
        return this;
    }

    public CaseFieldEntity addCaseACLEntities(final Collection<CaseFieldACLEntity> entities) {
        entities.forEach(e -> addCaseFieldACL(e));
        return this;
    }

    @Transient
    public boolean isMetadataField() {
        return dataFieldType == DataFieldType.METADATA;
    }
}
