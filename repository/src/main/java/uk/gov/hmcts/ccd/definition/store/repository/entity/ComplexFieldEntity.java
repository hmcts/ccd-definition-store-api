package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "complex_field")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")
)
public class ComplexFieldEntity implements FieldEntity, Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "hint")
    private String hint;

    @Column(name = "hidden")
    private Boolean hidden;

    @Column(name = "security_classification")
    @Type(type = "pgsql_securityclassification_enum")
    private SecurityClassification securityClassification;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "field_type_id", nullable = false)
    private FieldTypeEntity fieldType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complex_field_type_id", nullable = false)
    private FieldTypeEntity complexFieldType;

    @Column(name = "show_condition")
    private String showCondition;

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
}
