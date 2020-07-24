package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class FieldTypeBuilder {

    private String reference;
    private FieldTypeEntity baseFieldType;
    private List<ComplexFieldEntity> fieldsForComplex = newArrayList();
    private FieldTypeEntity fieldForCollection;

    private FieldTypeBuilder() {
    }

    public FieldTypeBuilder withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public FieldTypeBuilder addFieldToCollection(FieldTypeEntity fieldTypeEntity) {
        this.fieldForCollection = fieldTypeEntity;
        return this;
    }

    public FieldTypeBuilder addFieldToComplex(String reference, FieldTypeEntity fieldType) {
        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference(reference);
        field.setFieldType(fieldType);
        this.fieldsForComplex.add(field);
        return this;
    }

    public FieldTypeBuilder withBaseFieldType(FieldTypeEntity baseFieldType) {
        this.baseFieldType = baseFieldType;
        return this;
    }

    public FieldTypeBuilder withComplexField(ComplexFieldEntity field) {
        this.fieldsForComplex.add(field);
        return this;
    }

    public FieldTypeEntity build() {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference(this.reference);
        fieldType.setBaseFieldType(this.baseFieldType);
        fieldType.addComplexFields(fieldsForComplex);
        return fieldType;
    }

    public FieldTypeEntity buildComplex() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(this.reference);
        fieldTypeEntity.addComplexFields(fieldsForComplex);
        fieldTypeEntity.setBaseFieldType(newType("Complex").build());
        return fieldTypeEntity;
    }

    public FieldTypeEntity buildCollection() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(this.reference);
        fieldTypeEntity.setCollectionFieldType(fieldForCollection);
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

    public static FieldTypeEntity labelFieldType() {
        return newType("Label").build();
    }
}
