package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static javax.persistence.GenerationType.IDENTITY;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Table(name = "field_type")
@Entity
public class FieldTypeEntity implements Serializable, Versionable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "minimum")
    private String minimum;

    @Column(name = "maximum")
    private String maximum;

    @Column(name = "regular_expression")
    private String regularExpression = null;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "base_field_type_id")
    private FieldTypeEntity baseFieldType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collection_field_type_id")
    private FieldTypeEntity collectionFieldType;

    @OneToMany(mappedBy = "fieldType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final List<FieldTypeListItemEntity> listItems = new ArrayList<>();

    @OneToMany(mappedBy = "complexFieldType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("id")
    private final List<ComplexFieldEntity> complexFields = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jurisdiction_id")
    private JurisdictionEntity jurisdiction;

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(final String minimum) {
        this.minimum = minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public void setMaximum(final String maximum) {
        this.maximum = maximum;
    }

    public FieldTypeEntity getBaseFieldType() {
        return baseFieldType;
    }

    public void setBaseFieldType(FieldTypeEntity baseFieldType) {
        this.baseFieldType = baseFieldType;
    }

    public FieldTypeEntity getCollectionFieldType() {
        return collectionFieldType;
    }

    public void setCollectionFieldType(FieldTypeEntity collectionFieldType) {
        this.collectionFieldType = collectionFieldType;
    }

    public List<FieldTypeListItemEntity> getListItems() {
        return listItems;
    }

    public List<ComplexFieldEntity> getComplexFields() {
        return complexFields;
    }

    public boolean hasComplexField(String complexFieldReference) {
        return this.complexFields.stream().anyMatch(f -> f.getReference().equals(complexFieldReference));
    }

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(final JurisdictionEntity jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public void addComplexFields(List<ComplexFieldEntity> complexFields) {
        for (ComplexFieldEntity complexField : complexFields) {
            complexField.setComplexFieldType(this);
            this.complexFields.add(complexField);
        }
    }

    public void addListItems(List<FieldTypeListItemEntity> listItems) {
        for (FieldTypeListItemEntity listItem : listItems) {
            listItem.setFieldType(this);
            this.listItems.add(listItem);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("reference", reference)
                          .add("version", version)
                          .toString();
    }

    public static String uniqueReference(String id) {
        return String.format("%s-%s", id, UUID.randomUUID());
    }

}
