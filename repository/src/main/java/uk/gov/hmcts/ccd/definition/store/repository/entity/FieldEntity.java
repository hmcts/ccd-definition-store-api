package uk.gov.hmcts.ccd.definition.store.repository.entity;

public interface FieldEntity {

    String COMPLEX_TYPE_KEY = "Complex";
    String COLLECTION_TYPE_KEY = "Collection";

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

    default boolean isComplex() {
        return this.getBaseTypeString().equals(COMPLEX_TYPE_KEY);
    }

    default boolean isCollection() {
        return this.getBaseTypeString().equals(COLLECTION_TYPE_KEY);
    }
}
