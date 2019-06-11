package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

public class FieldTypeListItemBuilder {

    private FieldTypeListItemEntity fieldTypeListItemEntity = new FieldTypeListItemEntity();

    public FieldTypeListItemBuilder withLabel(String label) {
        fieldTypeListItemEntity.setLabel(label);
        return this;
    }

    public FieldTypeListItemBuilder withValue(String value) {
        fieldTypeListItemEntity.setValue(value);
        return this;
    }

    public FieldTypeListItemBuilder withFieldType(FieldTypeEntity fieldType) {
        fieldTypeListItemEntity.setFieldType(fieldType);
        return this;
    }

    public static FieldTypeListItemBuilder newType() {
        return new FieldTypeListItemBuilder();
    }

    public FieldTypeListItemEntity build() {
        return fieldTypeListItemEntity;
    }
}
