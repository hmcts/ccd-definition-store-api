package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

import static junit.framework.TestCase.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.labelFieldType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

public class CaseTypeEntityFieldLabelValidatorTest {

    CaseTypeEntityFieldLabelValidator caseTypeEntityFieldLabelValidator = new CaseTypeEntityFieldLabelValidator();

    @Test
    void shouldSuccessfullyValidatePlaceholderIfLastElementIsLeaf() {

        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setLabel("This is a value: ${complex.collection.nested.nested2.textField}");

        CaseTypeEntity caseType = new CaseTypeEntity();
        caseField.setReference("case field");
        caseType.getCaseFields().add(caseField);
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        Assertions.assertAll(
            () -> assertTrue(validate.isValid())
        );
    }

    @Test
    void shouldFailToValidatePlaceholderIfLastElementIsComplexType() {

        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setLabel("This is a value: ${complex.collection.nested.nested2.textField}");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        caseField = new CaseFieldEntity();
        caseField.setReference("case field");

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        Assertions.assertAll(
            () -> assertTrue(validate.isValid())
        );
    }

    private CaseFieldEntity newComplexFieldWithCollectionOfNestedComplexFields() {
        CaseFieldBuilder complexField = newField("complex", "collection");
        complexField.addFieldToComplex("field1", textFieldType());
        complexField.addFieldToComplex("field2", textFieldType());
        complexField.addFieldToComplex("field3", labelFieldType());

        FieldTypeBuilder collectionType = newType("collection");
        collectionType.addFieldToCollection(newType("nested")
                                                .addFieldToComplex("nested2",
                                                                   newType("textField").build())
                                                .buildComplex());
        complexField.addFieldToComplex("nestedComplexField", collectionType.buildCollection());

        return complexField.buildComplex();
    }

    private CaseFieldEntity newCollectionField() {
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
        complexType.addFieldToComplex("aLabel", labelFieldType());
        return complexType.buildComplex();
    }
}
