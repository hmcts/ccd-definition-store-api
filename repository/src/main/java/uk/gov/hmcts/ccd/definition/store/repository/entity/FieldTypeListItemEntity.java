package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Table(name = "field_type_list_item")
@Entity
public class FieldTypeListItemEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "field_type_list_item_id_seq")
    private Integer id;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "display_order")
    private Integer order;

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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
