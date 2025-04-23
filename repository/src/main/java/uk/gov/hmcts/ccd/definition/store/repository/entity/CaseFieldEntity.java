package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Table(name = "case_field")
@Entity
public class CaseFieldEntity implements FieldEntity, Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "case_field_id_seq")
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

    @Column(name = "searchable")
    private boolean searchable = true;

    @Column(name = "security_classification")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SecurityClassification securityClassification;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "field_type_id", nullable = false)
    private FieldTypeEntity fieldType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true, mappedBy = "caseField")
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<CaseFieldACLEntity> caseFieldACLEntities = new ArrayList<>();

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_field_id")
    private final List<ComplexFieldACLEntity> complexFieldACLEntities = new ArrayList<>();

    @Column(name = "data_field_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DataFieldType dataFieldType;

    @Column(name = "category_id")
    private String categoryId;

    @Transient
    private String oid = IdGenerator.createId();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public CaseFieldEntity() {
        this.dataFieldType = DataFieldType.CASE_DATA;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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


    public List<ComplexFieldACLEntity> getComplexFieldACLEntities() {
        return complexFieldACLEntities;
    }

    public CaseFieldEntity addComplexFieldACL(final ComplexFieldACLEntity complexFieldACLEntity) {
        complexFieldACLEntity.setCaseField(this);
        complexFieldACLEntities.add(complexFieldACLEntity);
        return this;
    }

    public CaseFieldEntity addComplexFieldACLEntities(final Collection<ComplexFieldACLEntity> entities) {
        entities.forEach(e -> addComplexFieldACL(e));
        return this;
    }

    @Override
    public boolean isMetadataField() {
        return dataFieldType == DataFieldType.METADATA;
    }

    @Transient
    public Optional<CaseFieldACLEntity> getCaseFieldACLByAccessProfile(String accessProfile) {
        return this.caseFieldACLEntities.stream().filter(e -> accessProfileEquals(accessProfile, e)).findFirst();
    }

    private boolean accessProfileEquals(String accessProfile, CaseFieldACLEntity e) {
        if (e.getAccessProfile() == null) {
            return false;
        }
        if (e.getAccessProfile().getReference() == null) {
            return false;
        }
        return e.getAccessProfile().getReference().equalsIgnoreCase(accessProfile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CaseFieldEntity that = (CaseFieldEntity) o;
        if (getId() != null) {
            return Objects.equals(getId(), that.getId());
        } else {
            return Objects.equals(getOid(), that.getOid());
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        } else {
            return Objects.hash(getOid());
        }
    }
}
