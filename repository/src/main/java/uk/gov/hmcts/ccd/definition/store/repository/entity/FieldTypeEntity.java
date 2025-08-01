package uk.gov.hmcts.ccd.definition.store.repository.entity;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COLLECTION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DOCUMENT;

@Table(name = "field_type")
@Entity
public class FieldTypeEntity implements Serializable, Versionable {

    private static final long serialVersionUID = -997923411806171504L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "field_type_id_seq")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_field_type_id")
    private FieldTypeEntity baseFieldType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_field_type_id")
    private FieldTypeEntity collectionFieldType;

    @OneToMany(mappedBy = "fieldType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final List<FieldTypeListItemEntity> listItems = new ArrayList<>();

    @OneToMany(mappedBy = "complexFieldType", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    @OrderBy("id")
    private final Set<ComplexFieldEntity> complexFields = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jurisdiction_id")
    private JurisdictionEntity jurisdiction;

    private static final Set<String> FIXED_List_ITEMS = new HashSet<>(Arrays.asList("FixedList",
        "MultiSelectList",
        "FixedRadioList"));

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

    public Set<ComplexFieldEntity> getComplexFields() {
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

    public static boolean isFixedList(String reference) {
        return FIXED_List_ITEMS.contains(reference);
    }

    @Transient
    public Set<ComplexFieldEntity> getChildren() {
        if (this.baseFieldType == null) {
            return emptySet();
        } else if (this.baseFieldType.getReference().equalsIgnoreCase(BASE_COMPLEX)) {
            return this.complexFields;
        } else if (this.baseFieldType.getReference().equalsIgnoreCase(BASE_COLLECTION)) {
            if (this.collectionFieldType == null) {
                return emptySet();
            }
            return collectionFieldType.complexFields;
        } else {
            return emptySet();
        }
    }

    @Transient
    public boolean isDocumentType() {
        return (BASE_DOCUMENT.equals(reference)
            || (baseFieldType != null && BASE_DOCUMENT.equals(baseFieldType.getReference())));
    }

    @Transient
    public boolean isCollectionFieldType() {
        return collectionFieldType != null;
    }

    @Transient
    public boolean isComplexFieldType() {
        FieldTypeEntity baseFieldType = this.baseFieldType.getBaseFieldType();
        if (baseFieldType != null) {
            return baseFieldType.getReference().equalsIgnoreCase(BASE_COMPLEX);
        } else {
            return this.baseFieldType.getReference().equalsIgnoreCase(BASE_COMPLEX);
        }
    }

}
