package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static javax.persistence.GenerationType.SEQUENCE;

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
