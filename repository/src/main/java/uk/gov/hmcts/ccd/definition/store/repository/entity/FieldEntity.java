package uk.gov.hmcts.ccd.definition.store.repository.entity;

public interface FieldEntity {

    String getReference();

    FieldTypeEntity getFieldType();

    //TODO add test
    default String getBaseTypeString() {
        FieldTypeEntity baseFieldType = this.getFieldType().getBaseFieldType();
        if (baseFieldType != null) {
            return baseFieldType.getReference();
        } else {
            return getFieldType().getReference();
        }
    }
}
