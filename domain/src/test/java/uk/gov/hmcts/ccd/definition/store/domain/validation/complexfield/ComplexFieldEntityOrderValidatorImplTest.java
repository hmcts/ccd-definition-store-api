package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.ComplexFieldBuilder.newComplexField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

class ComplexFieldEntityOrderValidatorImplTest {

    private ComplexFieldEntityOrderValidatorImpl complexFieldEntityOrderValidator = new ComplexFieldEntityOrderValidatorImpl();

    CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @Test
    void shouldPassIfNoElementsHaveDisplayOrderValues() {
        CaseFieldEntity caseField = newField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested1")
                                                     .withFieldType(newType("nested1FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("nested2FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .build())
                               .build())
            .withDataFieldType(DataFieldType.CASE_DATA)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(validate.getValidationErrors(), hasSize(0));
    }

    @Test
    void shouldFailIfSomeButNotAllElementsHaveDisplayOrderValues() {
        CaseFieldEntity caseField = newField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested1")
                                                     .withFieldType(newType("nested1FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(2)
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("nested2FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .build())
                               .build())
            .withDataFieldType(DataFieldType.CASE_DATA)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage", is("ComplexField with reference=fieldType1 must have ordering for all children defined. WorkSheet 'ComplexTypes'"))));
    }

    @Test
    void shouldPassIfAllElementsHaveDisplayOrderValues() {
        CaseFieldEntity caseField = newField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested1")
                                                     .withFieldType(newType("nested1FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(2)
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("nested2FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(1)
                                                     .build())
                               .build())
            .withDataFieldType(DataFieldType.CASE_DATA)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(validate.getValidationErrors(), hasSize(0));
    }

    @Test
    void shouldFailIfElementsAreNotInIncrementalOrder() {
        CaseFieldEntity caseField = newField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested1")
                                                     .withFieldType(newType("nested1FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(5)
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("nested2FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(1)
                                                     .build())
                               .withComplexField(newComplexField("nested3")
                                                     .withFieldType(newType("nested3FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(3)
                                                     .build())
                               .build())
            .withDataFieldType(DataFieldType.CASE_DATA)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=fieldType1 has incorrect order for nested fields. Order has to be incremental and start from 1. WorkSheet 'ComplexTypes'"))));
    }

    @Test
    void shouldFailIfElementsOrderDoesNotStartWithOne() {
        CaseFieldEntity caseField = newField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested1")
                                                     .withFieldType(newType("nested1FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(3)
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("nested2FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(2)
                                                     .build())
                               .build())
            .withDataFieldType(DataFieldType.CASE_DATA)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=fieldType1 has incorrect order for nested fields. Order has to be incremental and start from 1. WorkSheet 'ComplexTypes'"))));
    }

    @Test
    void shouldFailIfElementsOrderOnNestedLevelsHaveIncorrectOrders() {

        CaseFieldEntity caseField = newField("field1")
            .withFieldType(newType("type")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested1")
                                                     .withFieldType(newType("typeNested1")
                                                                        .withBaseFieldType(newType("typeNested1BaseType")
                                                                                               .withReference(BASE_COMPLEX)
                                                                                               .build())
                                                                        .withComplexField(newComplexField("nested1Field1")
                                                                                              .withFieldType(newType("typeNested1FieldType1")
                                                                                                                 .withReference(BASE_TEXT)
                                                                                                                 .build())
                                                                                              .withOrder(2)
                                                                                              .build())
                                                                        .withComplexField(newComplexField("nested1Field2")
                                                                                              .withFieldType(newType("typeNested1FieldType2")
                                                                                                                 .withReference(BASE_TEXT)
                                                                                                                 .build())
                                                                                              .withOrder(3)
                                                                                              .build())
                                                                        .build())
                                                     .withOrder(2)
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("typeNested2")
                                                                        .withBaseFieldType(newType("typeNested2BaseType")
                                                                                               .withReference(BASE_COMPLEX)
                                                                                               .build())
                                                                        .withComplexField(newComplexField("field21")
                                                                                              .withFieldType(newType("typeNested2FieldType1")
                                                                                                                 .withReference(BASE_TEXT)
                                                                                                                 .build())
                                                                                              .build())
                                                                        .withComplexField(newComplexField("field22")
                                                                                              .withFieldType(newType("typeNested2FieldType2")
                                                                                                                 .withReference(BASE_TEXT)
                                                                                                                 .build())
                                                                                              .withOrder(3)
                                                                                              .build())
                                                                        .build())
                                                     .withOrder(1)
                                                     .build())
                               .build())
            .withDataFieldType(DataFieldType.CASE_DATA)
            .build();

        ValidationResult result = complexFieldEntityOrderValidator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), hasSize(2));
        assertThat(result.getValidationErrors(),
                   hasItems(hasProperty("defaultMessage",
                                       is("ComplexField with reference=typeNested1 has incorrect order for nested fields. Order has to be incremental and start from 1. WorkSheet 'ComplexTypes'")),
                            hasProperty("defaultMessage",
                                        is("ComplexField with reference=typeNested2 must have ordering for all children defined. WorkSheet 'ComplexTypes'"))));
    }

}
