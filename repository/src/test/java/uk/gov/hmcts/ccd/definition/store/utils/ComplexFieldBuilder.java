package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class ComplexFieldBuilder {

    private final ComplexFieldEntity complexFieldEntity;

    public ComplexFieldBuilder(String reference) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        this.complexFieldEntity = complexFieldEntity;
    }

    public ComplexFieldBuilder withFieldType(FieldTypeEntity fieldType) {
        complexFieldEntity.setFieldType(fieldType);
        return this;
    }

    public ComplexFieldBuilder withOrder(int order) {
        complexFieldEntity.setOrder(order);
        return this;
    }

    public ComplexFieldBuilder withComplexFieldType(FieldTypeEntity complexFieldType) {
        complexFieldEntity.setComplexFieldType(complexFieldType);
        return this;
    }

    public ComplexFieldEntity build() {
        return complexFieldEntity;
    }

    public static ComplexFieldBuilder newComplexField(String reference) {
        ComplexFieldBuilder complexFieldBuilder = new ComplexFieldBuilder(reference);
        return complexFieldBuilder;
    }
}
