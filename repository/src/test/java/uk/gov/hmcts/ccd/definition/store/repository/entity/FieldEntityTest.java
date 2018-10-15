package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

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
        complexOfComplex.addFieldToComplex("executorPerson", textFieldType());
        return complexOfComplex.buildComplex();
    }

    private CaseFieldEntity newCollectionFieldOfBaseType() {
        FieldTypeEntity collectionFieldType = newType("reasons-51503ee8-ac6d-4b57-845e-4806332a9820")
                .addFieldToCollection(textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private CaseFieldEntity newCollectionOfComplexField() {
        FieldTypeEntity collectionFieldType = newType("reasons-51503ee8-ac6d-4b57-845e-4806332a9820")
                .addFieldToCollection(newComplexType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private FieldTypeEntity newComplexType() {
        FieldTypeBuilder complexType = newType("Person");
        complexType.addFieldToComplex("forename", textFieldType());
        complexType.addFieldToComplex("dob", newType("Date").build());
        return complexType.buildComplex();
    }
}
