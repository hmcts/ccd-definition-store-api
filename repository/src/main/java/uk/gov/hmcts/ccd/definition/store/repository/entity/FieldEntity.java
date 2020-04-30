package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASELINK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;

public interface FieldEntity extends Serializable {

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

    default boolean isPredefinedComplexType() {
        return isComplexFieldType()
            && (this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ADDRESS_GLOBAL)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ADDRESS_UK)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_CASELINK)
            || this.getFieldType().getReference().equalsIgnoreCase(PREDEFINED_COMPLEX_ORDER_SUMMARY));
    }

    boolean isMetadataField();

    default boolean isCompound() {
        return isCollectionFieldType() || isComplexFieldType();
    }

    default FieldTypeEntity getCollectionFieldType() {
        return getFieldType().getCollectionFieldType();
    }
}
