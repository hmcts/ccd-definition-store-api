package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

public class CaseFieldBuilder {

    private String reference;
    private String fieldTypeReference;
    private FieldTypeEntity fieldTypeEntity;
    private DataFieldType dataFieldType;
    private List<ComplexFieldEntity> fieldsForComplex = newArrayList();
    private boolean searchable = true;

    private CaseFieldBuilder(String reference) {
        this.reference = reference;
    }

    public CaseFieldBuilder withFieldTypeReference(String reference) {
        this.fieldTypeReference = reference;
        return this;
    }

    public CaseFieldBuilder withFieldType(FieldTypeEntity fieldType) {
        this.fieldTypeEntity = fieldType;
        return this;
    }

    public CaseFieldBuilder addFieldToComplex(String reference, FieldTypeEntity fieldType) {
        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference(reference);
        field.setFieldType(fieldType);
        this.fieldsForComplex.add(field);
        return this;
    }

    public CaseFieldBuilder withSearchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public CaseFieldEntity buildComplex() {
        CaseFieldEntity field = new CaseFieldEntity();
        field.setReference(this.reference);
        field.setSearchable(this.searchable);
        FieldTypeEntity typeEntity = newType(fieldTypeReference).build();
        typeEntity.addComplexFields(fieldsForComplex);
        FieldTypeEntity baseTypeEntity = newType("Complex").build();
        typeEntity.setBaseFieldType(baseTypeEntity);
        field.setFieldType(typeEntity);
        return field;
    }

    public CaseFieldBuilder withDataFieldType(DataFieldType dataFieldType) {
        this.dataFieldType = dataFieldType;
        return this;
    }

    public CaseFieldEntity build() {
        CaseFieldEntity field = new CaseFieldEntity();
        field.setReference(this.reference);
        field.setSearchable(this.searchable);
        if (fieldTypeEntity != null) {
            field.setFieldType(fieldTypeEntity);
        } else {
            field.setFieldType(newType(fieldTypeReference).build());
            field.setDataFieldType(dataFieldType);
        }
        return field;
    }

    public static CaseFieldBuilder newField(String reference, String fieldTypeReference) {
        CaseFieldBuilder caseFieldBuilder = new CaseFieldBuilder(reference);
        caseFieldBuilder.withFieldTypeReference(fieldTypeReference);
        return caseFieldBuilder;
    }

    public static CaseFieldBuilder newField(String reference) {
        CaseFieldBuilder caseFieldBuilder = new CaseFieldBuilder(reference);
        return caseFieldBuilder;
    }

    public static CaseFieldBuilder newTextField(String reference) {
        return newField(reference, "Text");
    }
}
