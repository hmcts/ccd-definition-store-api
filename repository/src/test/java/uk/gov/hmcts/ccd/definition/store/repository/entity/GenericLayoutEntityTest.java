package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.ComplexFieldBuilder.newComplexField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

class GenericLayoutEntityTest {

    private static final String COMPLEX = "Complex";
    private static final String PERSON = "Person";
    private static final String DEBTOR_DETAILS = "DebtorDetails";
    private static final String NAME = "Name";
    private static final String SURNAME = "Surname";

    private CaseFieldEntity debtorDetails;

    @Nested
    class IsSearchableTest {

        @BeforeEach
        void setUp() {
            debtorDetails = newField(DEBTOR_DETAILS, COMPLEX).buildComplex();
        }

        @Test
        void shouldReturnTrueWhenSimpleFieldIsSearchable() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, null);
            debtorDetails.setSearchable(true);

            boolean result = entity.isSearchable();

            assertTrue(result);
        }

        @Test
        void shouldReturnFalseWhenSimpleFieldIsNotSearchable() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, "");
            debtorDetails.setSearchable(false);

            boolean result = entity.isSearchable();

            assertFalse(result);
        }

        @Test
        void shouldReturnTrueWhenNestedFieldPathIsAllSearchable() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, "Person.Surname");
            setUpComplex(true, true, true);

            boolean result = entity.isSearchable();

            assertTrue(result);
        }

        @Test
        void shouldReturnFalseWhenTopLevelFieldIsNotSearchable() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, "Person.Surname");
            setUpComplex(false, true, true);

            boolean result = entity.isSearchable();

            assertFalse(result);
        }

        @Test
        void shouldReturnFalseWhenFirstLevelNestedFieldIsNotSearchable() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, "Person.Surname");
            setUpComplex(true, false, true);

            boolean result = entity.isSearchable();

            assertFalse(result);
        }

        @Test
        void shouldReturnFalseWhenSecondLevelNestedFieldIsNotSearchable() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, "Person.Surname");
            setUpComplex(true, true, false);

            boolean result = entity.isSearchable();

            assertFalse(result);
        }

        @Test
        void shouldErrorWhenUnknownFieldIsInPath() {
            GenericLayoutEntity entity = layoutEntity(debtorDetails, "Person.UnknownField");
            setUpComplex(true, true, true);

            NullPointerException exception = assertThrows(NullPointerException.class, () -> entity.isSearchable());

            assertEquals(
                "Unable to find nested field 'UnknownField' within field 'Person'.", exception.getMessage());
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

    @Nested
    class BuildFieldPathTest {

        @Test
        void shouldBuildFieldPathWithCaseFieldElementPath() {
            GenericLayoutEntity layoutEntity = layoutEntity(DEBTOR_DETAILS, "Person.Name");

            String result = layoutEntity.buildFieldPath();

            assertEquals("DebtorDetails.Person.Name", result);
        }

        @Test
        void shouldBuildFieldPathWithEmptyCaseFieldElementPath() {
            GenericLayoutEntity layoutEntity = layoutEntity(DEBTOR_DETAILS, "");

            String result = layoutEntity.buildFieldPath();

            assertEquals("DebtorDetails", result);
        }

        @Test
        void shouldBuildFieldPathWithNullCaseFieldElementPath() {
            GenericLayoutEntity layoutEntity = layoutEntity(DEBTOR_DETAILS, null);

            String result = layoutEntity.buildFieldPath();

            assertEquals("DebtorDetails", result);
        }

        private GenericLayoutEntity layoutEntity(String topLevelFieldId, String caseFieldElementPath) {
            GenericLayoutEntity layoutEntity = new SearchInputCaseFieldEntity();
            layoutEntity.setCaseFieldElementPath(caseFieldElementPath);
            layoutEntity.setCaseField(newField(topLevelFieldId).build());
            return layoutEntity;
        }
    }
}
