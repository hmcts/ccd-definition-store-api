package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DOCUMENT;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;
import static uk.gov.hmcts.ccd.definition.store.utils.ComplexFieldBuilder.newComplexField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;


class ComplexFieldEntityOrderValidatorImplTest {

    private ComplexFieldEntityOrderValidatorImpl complexFieldEntityOrderValidator = new ComplexFieldEntityOrderValidatorImpl();

    CaseFieldComplexFieldEntityValidator.ValidationContext validationContext;

    @Test
    void shouldPassIfNoElementsHaveDisplayOrderValues() {
        ComplexFieldEntity caseField = newComplexField("field1")
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
            .withComplexFieldType(newType("parentType")
                                      .withBaseFieldType(newType(BASE_COMPLEX).build())
                                      .withComplexField(newComplexField("field1")
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
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(0));
    }

    @Test
    void shouldFailIfSingleTopLevelElementHasDisplayOrderNotStartingWithOne() {
        ComplexFieldEntity caseField = newComplexField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_DOCUMENT).build())
                               .build())
            .withComplexFieldType(aParentType())
            .withOrder(2)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=field1 has incorrect order for nested fields. Order has to be incremental and start from 1"))));
    }

    @Test
    void shouldFailIfSomeButNotAllElementsHaveDisplayOrderValues() {
        ComplexFieldEntity caseField = newComplexField("field1")
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
            .withComplexFieldType(aParentType())
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=field1 must have ordering for all children defined"))));
    }

    @Test
    void shouldPassIfAllElementsHaveDisplayOrderValues() {
        ComplexFieldEntity caseField = newComplexField("field1")
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
            .withComplexFieldType(aParentType())
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(0));
    }

    @Test
    void shouldFailIfOrderOfParentElementNotWithinSizeOfComplexFieldType() {
        ComplexFieldEntity caseField = newComplexField("field1")
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
            .withComplexFieldType(aParentType())
            .withOrder(2)
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=field1 has incorrect order for nested fields. Order has to be incremental and start from 1"))));
    }

    @Test
    void shouldFailIfElementsAreNotInIncrementalOrder() {
        ComplexFieldEntity caseField = newComplexField("field1")
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
            .withComplexFieldType(aParentType())
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=field1 has incorrect order for nested fields. Order has to be incremental and start from 1"))));
    }

    @Test
    void shouldFailIfElementsOrderDoesNotStartWithOne() {
        ComplexFieldEntity caseField = newComplexField("field1")
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
            .withComplexFieldType(aParentType())
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=field1 has incorrect order for nested fields. Order has to be incremental and start from 1"))));
    }

    @Test
    void shouldFailIfSingleElementOrderDoesNotStartWithOne() {
        ComplexFieldEntity caseField = newComplexField("field1")
            .withFieldType(newType("fieldType1")
                               .withBaseFieldType(newType(BASE_COMPLEX).build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("nested2FieldType1")
                                                                        .withReference(BASE_TEXT)
                                                                        .build())
                                                     .withOrder(2)
                                                     .build())
                               .build())
            .withComplexFieldType(aParentType())
            .build();

        ValidationResult validate = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(validate.getValidationErrors(), hasSize(1));
        assertThat(validate.getValidationErrors(),
                   hasItem(hasProperty("defaultMessage",
                                       is("ComplexField with reference=field1 has incorrect order for nested fields. Order has to be incremental and start from 1"))));
    }

    @Test
    void shouldFailIfElementsOrderOnNestedLevelsHaveIncorrectOrders() {

        ComplexFieldEntity caseField = newComplexField("field1")
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
                                                     .withOrder(3)
                                                     .build())
                               .withComplexField(newComplexField("nested2")
                                                     .withFieldType(newType("typeNested2")
                                                                        .withBaseFieldType(newType("typeNested2BaseType")
                                                                                               .withReference(BASE_COMPLEX)
                                                                                               .build())
                                                                        .withComplexField(newComplexField("nested2Field1")
                                                                                              .withFieldType(newType("typeNested2FieldType1")
                                                                                                                 .withReference(BASE_TEXT)
                                                                                                                 .build())
                                                                                              .build())
                                                                        .withComplexField(newComplexField("nested2Field2")
                                                                                              .withFieldType(newType("typeNested2FieldType2")
                                                                                                                 .withReference(BASE_TEXT)
                                                                                                                 .build())
                                                                                              .withOrder(3)
                                                                                              .build())
                                                                        .build())
                                                     .withOrder(1)
                                                     .build())
                               .build())
            .withComplexFieldType(aParentType())
            .build();

        ValidationResult result = complexFieldEntityOrderValidator.validate(caseField, validationContext);

        assertThat(result.getValidationErrors(), hasSize(3));
        assertThat(result.getValidationErrors(),
                   containsInAnyOrder(hasProperty("defaultMessage",
                                                  is("ComplexField with reference=field1 has incorrect order for nested fields. Order has to be incremental and start from 1")),
                                      hasProperty("defaultMessage",
                                                  is("ComplexField with reference=nested1 has incorrect order for nested fields. Order has to be incremental and start from 1")),
                                      hasProperty("defaultMessage",
                                                  is("ComplexField with reference=nested2 must have ordering for all children defined"))));
    }

    private FieldTypeEntity aParentType() {
        return newType("parentType")
            .withBaseFieldType(newType(BASE_COMPLEX).build())
            .withComplexField(newComplexField("field1")
                                  .withFieldType(newType("nested1FieldType1")
                                                     .withReference(BASE_TEXT)
                                                     .build())
                                  .build())
            .build();
    }

}
