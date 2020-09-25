package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.labelFieldType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

public class CaseTypeEntityFieldLabelValidatorTest {

    CaseTypeEntity caseType = new CaseTypeEntity();

    CaseFieldEntity caseField = new CaseFieldEntity();
    CaseTypeEntityFieldLabelValidator caseTypeEntityFieldLabelValidator = new CaseTypeEntityFieldLabelValidator();

    @BeforeEach
    void setUp() {
        caseField.setReference("case field");
        caseType.getCaseFields().add(caseField);
    }

    @Test
    @DisplayName("should successfully validate if no case fields")
    void shouldSuccessfullyValidatePlaceholderIfNoCaseFields() {
        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        assertTrue(validate.isValid());
    }

    @Test
    @DisplayName("should successfully validate placeholder if leaf element is simple type")
    void shouldSuccessfullyValidatePlaceholderIfLeafElementIsSimpleType() {
        caseField.setLabel("This is a value: ${complex.collection.nested.nested2.textField}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        assertTrue(validate.isValid());

        caseField.setLabel("This is a value: ${complex.collection.nested22}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        assertTrue(validate.isValid());

        caseField.setLabel("This is a value: ${complex.collection.nested23}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        assertTrue(validate.isValid());
    }

    @DisplayName("should successfully validate placeholder if leaf element is referring metadata field")
    void shouldSuccessfullyValidatePlaceholderIfLeafElementIsReferringMetadataField() {
        caseField.setLabel("This is a value: ${[CASE_REFERENCE]}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        assertTrue(validate.isValid());
    }

    @DisplayName("should successfully validate placeholder if leaf element contains reference to document filename")
    void shouldSuccessfullyValidatePlaceholderIfLeafContainsReferenceToExceptionalValue() {
        caseField.setLabel("This is a value: ${complex.collection.nested.nested2.document_filename}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        assertTrue(validate.isValid());
    }

    @Test
    @DisplayName("should fail to validate placeholder if leaf element is complex type")
    void shouldFailToValidatePlaceholderIfLeafElementIsComplexType() {
        caseField.setLabel("This is a value: ${complex.collection.nested.nested2}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        Assertions.assertAll(
            () -> assertFalse(validate.isValid()),
            () -> assertTrue(validate.getValidationErrors().get(0)
                instanceof CaseTypeEntityFieldLabelValidator.PlaceholderLeafNotSimpleTypeValidationError),
            () -> assertThat(validate.getValidationErrors(), hasItems(hasProperty("defaultMessage",
                is("Label of caseField 'case field' has placeholder "
                    + "'complex.collection.nested.nested2' that points to "
                    + "case field 'nested2' of non simple type"))))
        );
    }

    @Test
    @DisplayName("should fail to validate placeholder if leaf element is collection type")
    void shouldFailToValidatePlaceholderIfLeafElementIsCollectionType() {
        caseField.setLabel("This is a value: ${complex.collection}");
        caseType.getCaseFields().add(newComplexFieldWithCollectionOfNestedComplexFields());

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        Assertions.assertAll(
            () -> assertTrue(!validate.isValid()),
            () -> assertTrue(validate.getValidationErrors().get(0)
                instanceof CaseTypeEntityFieldLabelValidator.PlaceholderLeafNotSimpleTypeValidationError),
            () -> assertThat(validate.getValidationErrors(), hasItems(hasProperty("defaultMessage",
                is("Label of caseField 'case field' has placeholder 'complex.collection' "
                    + "that points to case field 'collection' of non simple type"))))
        );
    }

    @Test
    @DisplayName("should fail to validate placeholder if field id not found")
    void shouldFailToValidatePlaceholderIfFieldIdNotFound() {
        caseField.setLabel("This is a value: ${complex.fieldNotFound}");

        ValidationResult validate = caseTypeEntityFieldLabelValidator.validate(caseType);

        Assertions.assertAll(
            () -> assertTrue(!validate.isValid()),
            () -> assertTrue(validate.getValidationErrors().get(0)
                instanceof CaseTypeEntityFieldLabelValidator.PlaceholderCannotBeResolvedValidationError),
            () -> assertThat(validate.getValidationErrors(), hasItems(hasProperty("defaultMessage",
                is("Label of caseField 'case field' has placeholder 'complex.fieldNotFound' "
                    + "that points to unknown case field"))))
        );
    }

    private CaseFieldEntity newComplexFieldWithCollectionOfNestedComplexFields() {
        CaseFieldBuilder complexField = newField("complex", "collection");
        complexField.addFieldToComplex("field1", textFieldType());
        complexField.addFieldToComplex("field2", textFieldType());
        complexField.addFieldToComplex("field3", labelFieldType());
        complexField.addFieldToComplex("collection", newType("nested")
            .addFieldToCollection(newType("someCollectionReference")
                .addFieldToComplex("nested", newType("nested")
                    .addFieldToComplex("nested2", newType("nested2")
                        .addFieldToComplex("textField", newType("textField")
                            .build())
                        .buildComplex())
                    .buildComplex())
                .addFieldToComplex("nested22", newType("nested22").build())
                .addFieldToComplex("nested23", newType("nested23").build())
                .buildComplex())
            .buildCollection());

        return complexField.buildComplex();
    }
}
