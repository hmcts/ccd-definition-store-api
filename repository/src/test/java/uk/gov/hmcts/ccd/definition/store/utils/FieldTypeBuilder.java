package uk.gov.hmcts.ccd.definition.store.utils;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class FieldTypeBuilder {

    private String reference;
    private List<ComplexFieldEntity> complexFields = newArrayList();
    private FieldTypeEntity collectionFieldType;

    private FieldTypeBuilder() {}

    public FieldTypeBuilder withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public FieldTypeBuilder withCollectionFieldType(FieldTypeEntity fieldTypeEntity) {
        this.collectionFieldType = fieldTypeEntity;
        return this;
    }

    public FieldTypeBuilder addComplexField(String reference, FieldTypeEntity fieldType) {
        ComplexFieldEntity complexField = new ComplexFieldEntity();
        complexField.setReference(reference);
        complexField.setFieldType(fieldType);
        this.complexFields.add(complexField);
        return this;
    }

    public FieldTypeEntity build() {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(this.reference);
        return fieldType;
    }

    public FieldTypeEntity buildComplex() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(this.reference);
        fieldTypeEntity.addComplexFields(complexFields);
        fieldTypeEntity.setBaseFieldType(newType("Complex").build());
        return fieldTypeEntity;
    }

    public FieldTypeEntity buildCollection() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(this.reference);
        fieldTypeEntity.setCollectionFieldType(collectionFieldType);
        fieldTypeEntity.setBaseFieldType(newType("Collection").build());
        return fieldTypeEntity;
    }

    public static FieldTypeBuilder newType(String reference) {
        FieldTypeBuilder builder = new FieldTypeBuilder();
        builder.withReference(reference);
        return builder;
    }

    public static FieldTypeEntity textFieldType() {
        return newType("Text").build();
    }
}
