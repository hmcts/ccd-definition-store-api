package uk.gov.hmcts.ccd.definition.store.repository.entity;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;

public interface FieldEntity {

    String getReference();

    FieldTypeEntity getFieldType();

    default String getBaseTypeString() {
        FieldTypeEntity baseFieldType = this.getFieldType().getBaseFieldType();
        if (baseFieldType != null) {
            return baseFieldType.getReference();
        } else {
            return getFieldType().getReference();
        }
    }

    default FieldTypeEntity getBaseType() {
        return ofNullable(this.getFieldType().getBaseFieldType()).orElse(this.getFieldType());
    }

    default boolean isCollectionFieldType() {
        return this.getFieldType().getCollectionFieldType() != null;
    }

    default boolean isCollectionOfComplex() {
        FieldTypeEntity collectionFieldType = this.getFieldType().getCollectionFieldType();
        return collectionFieldType != null && !collectionFieldType.getComplexFields().isEmpty();
    }

    default boolean isComplexFieldType() {
        return !isMetadataField() && this.getBaseTypeString().equalsIgnoreCase(BASE_COMPLEX);
    }

    boolean isMetadataField();
}
