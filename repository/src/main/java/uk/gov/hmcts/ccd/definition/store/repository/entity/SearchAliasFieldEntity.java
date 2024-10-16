package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "case_field_path", nullable = false)
    private String caseFieldPath;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "field_type_id", nullable = false)
    private FieldTypeEntity fieldType;

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public String getCaseFieldPath() {
        return caseFieldPath;
    }

    public void setCaseFieldPath(String caseFieldPath) {
        this.caseFieldPath = caseFieldPath;
    }

    public FieldTypeEntity getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldTypeEntity fieldType) {
        this.fieldType = fieldType;
    }
}
