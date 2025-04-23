package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Transient;

@Table(name = "complex_field")
@Entity
public class ComplexFieldEntity implements FieldEntity, Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "complex_field_id_seq")
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "label", nullable = false)
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "field_type_id", nullable = false)
    private FieldTypeEntity fieldType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complex_field_type_id", nullable = false)
    private FieldTypeEntity complexFieldType;

    @Column(name = "show_condition")
    private String showCondition;

    @Column(name = "display_order")
    private Integer order;

    @Column(name = "display_context_parameter")
    private String displayContextParameter;

    @Column(name = "retain_hidden_value")
    private Boolean retainHiddenValue;

    @Column(name = "category_id")
    private String categoryId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    @Transient
    @Override
    public boolean isMetadataField() {
        return false;
    }

    @Transient
    private String oid = IdGenerator.createId();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setFieldType(FieldTypeEntity fieldType) {
        this.fieldType = fieldType;
    }

    public FieldTypeEntity getComplexFieldType() {
        return complexFieldType;
    }

    public void setComplexFieldType(FieldTypeEntity complexFieldType) {
        this.complexFieldType = complexFieldType;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    public String getShowCondition() {
        return showCondition;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }

    public Boolean getRetainHiddenValue() {
        return retainHiddenValue;
    }

    public void setRetainHiddenValue(Boolean retainHiddenValue) {
        this.retainHiddenValue = retainHiddenValue;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComplexFieldEntity that = (ComplexFieldEntity) o;
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
