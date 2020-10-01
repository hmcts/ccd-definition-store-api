package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.ComplexFieldBuilder.newComplexField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

class CaseFieldEntityTest {

    private static final String COMPLEX = "Complex";
    private static final String PERSON = "Person";
    private static final String DEBTOR_DETAILS = "Debtor details";
    private static final String NAME = "Name";
    private static final String SURNAME = "Surname";

    private CaseFieldEntity debtorDetails;

    @Nested
    @DisplayName("find by path tests")
    class FindNestedElementsTest {

        @BeforeEach
        void setUp() {
            debtorDetails = newField(DEBTOR_DETAILS, "Complex")
                .addFieldToComplex(PERSON, newType(PERSON)
                    .withBaseFieldType(newType(COMPLEX).build())
                    .addFieldToComplex(NAME, newType(NAME).build())
                    .addFieldToComplex(SURNAME, newType(SURNAME).build())
                    .buildComplex())
                .buildComplex();
        }

        @Test
        void shouldFindNestedElementByPath() {
            String path = PERSON + "." + NAME;
            Optional<FieldEntity> nestedElementByPath = debtorDetails.findNestedElementByPath(path);

            assertAll(
                () -> assertTrue(nestedElementByPath.isPresent()),
                () -> assertThat(nestedElementByPath.get().getReference(), is(NAME)),
                () -> assertThat(nestedElementByPath.get().getFieldType().getBaseFieldType(), is(nullValue())),
                () -> assertThat(nestedElementByPath.get().getFieldType().getReference(), is(NAME)),
                () -> assertThat(nestedElementByPath.get().getFieldType().getChildren().size(), is(0)));
        }

        @Test
        void shouldFindNestedElementForCaseFieldWithEmptyPath() {
            Optional<FieldEntity> nestedElementByPath = debtorDetails.findNestedElementByPath("");
            assertEquals(debtorDetails, nestedElementByPath.get());
        }

        @Test
        void shouldFindNestedElementForCaseFieldWithNullPath() {
            Optional<FieldEntity> nestedElementByPath = debtorDetails.findNestedElementByPath(null);
            assertEquals(debtorDetails, nestedElementByPath.get());
        }

        @Test
        void shouldFailToFindNestedElementForCaseFieldWithNoNestedElements() {
            String path = "Field1";
            CaseFieldEntity nameField = newField(NAME, "Text").build();
            Optional<FieldEntity> nestedElementByPath = nameField.findNestedElementByPath(path);
            assertFalse(nestedElementByPath.isPresent(), "CaseField Name has no nested elements.");
        }

        @Test
        void shouldFailToFindNestedElementForCaseFieldWithNonMatchingPathElement() {
            String path = "Case";
            Optional<FieldEntity> nestedElementByPath = debtorDetails.findNestedElementByPath(path);
            assertFalse(nestedElementByPath.isPresent(), "Nested element not found for " + path);
        }

        @Test
        void shouldFailToFindNestedElementForCaseFieldWithNonMatchingPathElements() {
            String path = PERSON + "." + "Address";
            Optional<FieldEntity> nestedElementByPath = debtorDetails.findNestedElementByPath(path);
            assertFalse(nestedElementByPath.isPresent(), "Nested element not found for " + path);
        }

    }

    @Nested
    class IsNestedFieldSearchableTest {

        @BeforeEach
        void setUp() {
            debtorDetails = newField(DEBTOR_DETAILS, COMPLEX).buildComplex();
        }

        @Test
        void shouldReturnTrueWhenSimpleFieldIsSearchable() {
            debtorDetails.setSearchable(true);

            boolean result = debtorDetails.isNestedFieldSearchable(null);

            assertTrue(result);
        }

        @Test
        void shouldReturnFalseWhenSimpleFieldIsNotSearchable() {
            debtorDetails.setSearchable(false);

            boolean result = debtorDetails.isNestedFieldSearchable("");

            assertFalse(result);
        }

        @Test
        void shouldReturnTrueWhenNestedFieldPathIsAllSearchable() {
            setUpComplex(true, true, true);

            boolean result = debtorDetails.isNestedFieldSearchable("Person.Surname");

            assertTrue(result);
        }

        @Test
        void shouldReturnFalseWhenTopLevelFieldIsNotSearchable() {
            setUpComplex(false, true, true);

            boolean result = debtorDetails.isNestedFieldSearchable("Person.Surname");

            assertFalse(result);
        }

        @Test
        void shouldReturnFalseWhenFirstLevelNestedFieldIsNotSearchable() {
            setUpComplex(true, false, true);

            boolean result = debtorDetails.isNestedFieldSearchable("Person.Surname");

            assertFalse(result);
        }

        @Test
        void shouldReturnFalseWhenSecondLevelNestedFieldIsNotSearchable() {
            setUpComplex(true, true, false);

            boolean result = debtorDetails.isNestedFieldSearchable("Person.Surname");

            assertFalse(result);
        }

        @Test
        void shouldErrorWhenUnknownFieldIsInPath() {
            setUpComplex(true, true, true);

            NullPointerException exception = assertThrows(NullPointerException.class,
                () -> debtorDetails.isNestedFieldSearchable("Person.UnknownField"));

            assertEquals("Unable to find nested field 'UnknownField' within field 'Person'.",
                exception.getMessage());
        }

        private void setUpComplex(boolean topLevelFieldSearchable,
                                  boolean firstLevelNestedFieldSearchable,
                                  boolean secondLevelNestedFieldSearchable) {
            debtorDetails.setSearchable(topLevelFieldSearchable);
            debtorDetails.getFieldType().addComplexFields(singletonList(newComplexField(PERSON)
                .withSearchable(firstLevelNestedFieldSearchable)
                .withFieldType(newType(COMPLEX)
                    .withComplexField(newComplexField(SURNAME)
                        .withSearchable(secondLevelNestedFieldSearchable)
                        .build())
                    .withComplexField(newComplexField(NAME)
                        .build())
                    .buildComplex())
                .build()
            ));
        }

        private GenericLayoutEntity layoutEntity(CaseFieldEntity caseFieldEntity, String fieldElementPath) {
            GenericLayoutEntity entity = new SearchInputCaseFieldEntity();
            entity.setCaseField(caseFieldEntity);
            entity.setCaseFieldElementPath(fieldElementPath);
            return entity;
        }
    }
}
