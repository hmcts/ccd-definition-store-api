package uk.gov.hmcts.ccd.definition.store.repository.entity;

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

    default boolean isCollectionOfComplex() {
        FieldTypeEntity collectionFieldType = this.getFieldType().getCollectionFieldType();
        return collectionFieldType != null && !collectionFieldType.getComplexFields().isEmpty();
    }

    default boolean isComplexFieldType() {
        return this.getBaseTypeString().equalsIgnoreCase(BASE_COMPLEX);
    }
}
