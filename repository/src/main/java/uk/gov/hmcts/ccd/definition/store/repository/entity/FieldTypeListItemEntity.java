package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "field_type_list_item")
@Entity
public class FieldTypeListItemEntity implements DefEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "label", nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_type_id", nullable = false)
    private FieldTypeEntity fieldType;

    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public FieldTypeEntity getFieldType() {
        return fieldType;
    }

    public void setFieldType(final FieldTypeEntity fieldType) {
        this.fieldType = fieldType;
    }
}
