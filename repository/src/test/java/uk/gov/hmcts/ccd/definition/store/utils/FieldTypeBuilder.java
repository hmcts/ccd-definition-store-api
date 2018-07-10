package uk.gov.hmcts.ccd.definition.store.utils;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class FieldTypeBuilder {

    private String reference;
    private List<ComplexFieldEntity> complexFields = newArrayList();
    private FieldTypeEntity collectionFieldType;

    public FieldTypeBuilder() {}

    public static FieldTypeEntity baseFieldType(String reference) {
        FieldTypeEntity baseType = new FieldTypeEntity();
        baseType.setReference(reference);
        return baseType;
    }

    public static FieldTypeEntity textFieldType() {
        return baseFieldType("Text");
    }

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

    public FieldTypeEntity buildComplex() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(this.reference);
        fieldTypeEntity.addComplexFields(complexFields);
        fieldTypeEntity.setBaseFieldType(new FieldTypeBuilder().withReference("Complex").build());
        return fieldTypeEntity;
    }

    public FieldTypeEntity build() {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(this.reference);
        return fieldType;
    }

    public FieldTypeEntity buildCollection() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(this.reference);
        fieldTypeEntity.setCollectionFieldType(collectionFieldType);
        fieldTypeEntity.setBaseFieldType(new FieldTypeBuilder().withReference("Collection").build());
        return fieldTypeEntity;
    }
}
