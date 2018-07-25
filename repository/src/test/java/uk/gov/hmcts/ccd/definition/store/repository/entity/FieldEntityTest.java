package uk.gov.hmcts.ccd.definition.store.repository.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

public class FieldEntityTest {

    @Test
    public void testBaseTypeString() {
        CaseFieldEntity baseTypeField = newTextField("forename").build();
        CaseFieldEntity complexField = newComplexField();
        CaseFieldEntity collection = newCollectionFieldOfBaseType();

        assertThat(baseTypeField.getBaseTypeString(), equalTo("Text"));
        assertThat(complexField.getBaseTypeString(), equalTo("Complex"));
        assertThat(collection.getBaseTypeString(), equalTo("Collection"));
    }

    @Test
    public void testIsCollectionOfComplex() {
        CaseFieldEntity baseTypeField = newTextField("forename").build();
        CaseFieldEntity complexField = newComplexField();
        CaseFieldEntity collection = newCollectionFieldOfBaseType();
        CaseFieldEntity collectionOfComplex = newCollectionOfComplexField();

        assertThat(baseTypeField.isCollectionOfComplex(), equalTo(false));
        assertThat(complexField.isCollectionOfComplex(), equalTo(false));
        assertThat(collection.isCollectionOfComplex(), equalTo(false));
        assertThat(collectionOfComplex.isCollectionOfComplex(), equalTo(true));
    }

    private CaseFieldEntity newComplexField() {
        CaseFieldBuilder complexOfComplex = newField("executor", "Executor");
        complexOfComplex.withComplexField("executorPerson", FieldTypeBuilder.textFieldType());
        return complexOfComplex.buildComplex();
    }

    private CaseFieldEntity newCollectionFieldOfBaseType() {
        FieldTypeEntity collectionFieldType = new FieldTypeBuilder().withReference
                ("reasons-51503ee8-ac6d-4b57-845e-4806332a9820").withCollectionFieldType(FieldTypeBuilder
                .textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private CaseFieldEntity newCollectionOfComplexField() {
        FieldTypeEntity collectionFieldType = new FieldTypeBuilder().withReference
                ("reasons-51503ee8-ac6d-4b57-845e-4806332a9820").withCollectionFieldType(newComplexType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private FieldTypeEntity newComplexType() {
        FieldTypeBuilder complexType = new FieldTypeBuilder().withReference("Person");
        complexType.addComplexField("forename", FieldTypeBuilder.textFieldType());
        complexType.addComplexField("dob", FieldTypeBuilder.baseFieldType("Date"));
        return complexType.buildComplex();
    }
}