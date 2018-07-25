package uk.gov.hmcts.ccd.definition.store.utils;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ObjectArrays.newArray;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class CaseFieldBuilder {

    private String reference;
    private String fieldTypeReference;
    private List<ComplexFieldEntity> complexFields = newArrayList();

    public CaseFieldBuilder(String reference) {
        this.reference = reference;
    }

    public CaseFieldBuilder withFieldTypeReference(String reference) {
        this.fieldTypeReference = reference;
        return this;
    }

    public CaseFieldEntity build() {
        CaseFieldEntity field = new CaseFieldEntity();
        field.setReference(this.reference);
        field.setFieldType(new FieldTypeBuilder().withReference(fieldTypeReference).build());
        return field;
    }

    public CaseFieldBuilder withComplexField(String reference, FieldTypeEntity fieldType) {
        ComplexFieldEntity complexField = new ComplexFieldEntity();
        complexField.setReference(reference);
        complexField.setFieldType(fieldType);
        this.complexFields.add(complexField);
        return this;
    }

    public CaseFieldEntity buildComplex() {
        CaseFieldEntity field = new CaseFieldEntity();
        field.setReference(this.reference);
        FieldTypeEntity typeEntity = new FieldTypeBuilder().withReference(fieldTypeReference).build();
        typeEntity.addComplexFields(complexFields);
        FieldTypeEntity baseTypeEntity = new FieldTypeBuilder().withReference("Complex").build();
        typeEntity.setBaseFieldType(baseTypeEntity);
        field.setFieldType(typeEntity);
        return field;
    }

    public static CaseFieldBuilder newField(String reference, String fieldTypeReference) {
        CaseFieldBuilder caseFieldBuilder = new CaseFieldBuilder(reference);
        caseFieldBuilder.withFieldTypeReference(fieldTypeReference);
        return caseFieldBuilder;
    }

    public static CaseFieldBuilder newTextField(String reference) {
        return newField(reference, "Text");
    }
}
