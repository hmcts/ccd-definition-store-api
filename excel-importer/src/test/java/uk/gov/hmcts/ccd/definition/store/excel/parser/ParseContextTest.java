package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ParseContextTest {

    private ParseContext parseContext;

    @BeforeEach
    public void setUp() {
        parseContext = new ParseContext();
    }

    @Test
    void shouldFail_whenRegisterCaseFieldTypeAgainForCaseType() {

        final CaseFieldEntity caseField = mock(CaseFieldEntity.class);
        given(caseField.getReference()).willReturn("case field id");

        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);
        }, "Case field already registered for ID: case field id");
    }

    @Test
    void shouldRegisterAndGetCaseFieldForCaseType() {

        final CaseFieldEntity caseField = mock(CaseFieldEntity.class);
        given(caseField.getReference()).willReturn("case field id");

        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        final CaseFieldEntity retrieved = parseContext.getCaseFieldForCaseType("caseTypeId", "case field id");
        assertThat(retrieved, is(caseField));
    }

    @Test
    void shouldRegisterAndGetCaseFieldType() {

        final FieldTypeEntity fieldType = mock(FieldTypeEntity.class);
        parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);

        final FieldTypeEntity retrieved = parseContext.getCaseFieldType("caseTypeId", "fieldId");

        assertThat(retrieved, is(fieldType));
    }

    @Test
    void shouldFail_whenRegisterCaseFieldTypeAgain() {

        final FieldTypeEntity fieldType = mock(FieldTypeEntity.class);
        parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);

        assertThrows(SpreadsheetParsingException.class, () -> {
                parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);
            }, "Type already registered for field: fieldId"
        );
    }

    @Test
    void shouldRegisterAndGetStateForCaseType() {

        final StateEntity state = mock(StateEntity.class);
        given(state.getReference()).willReturn("some state id");
        parseContext.registerStateForCaseType("case type id", state);

        final StateEntity retrieved = parseContext.getStateForCaseType("case type id", "some state id");

        assertThat(retrieved, is(state));

    }

    @Test
    void shouldFail_whenRegisterStateForCaseTypeAgain() {

        final StateEntity state = mock(StateEntity.class);
        given(state.getReference()).willReturn("some state id");
        parseContext.registerStateForCaseType("case type id", state);

        assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerStateForCaseType("case type id", state);
        }, "State already registered for ID: some state id");
    }

    @Test
    void shouldSetAndGetJurisdiction() {

        final JurisdictionEntity j = mock(JurisdictionEntity.class);
        parseContext.setJurisdiction(j);

        assertThat(parseContext.getJurisdiction(), is(j));
    }

    @Test
    void shouldRegisterAndGetCaseType() {
        final CaseTypeEntity t1 = mock(CaseTypeEntity.class);
        final CaseTypeEntity t2 = mock(CaseTypeEntity.class);

        parseContext.registerCaseType(t1);
        parseContext.registerCaseType(t2);
        parseContext.registerCaseType(t1);

        final Set<CaseTypeEntity> caseTypes = parseContext.getCaseTypes();
        assertThat(caseTypes.size(), is(2));
        assertThat(caseTypes, containsInAnyOrder(t1, t2));
    }

    @Test
    void shouldFail_whenCaseTypeNotRegistered() {
        assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.getCaseFieldType("ngitb", "case field");
        }, "No types registered for case type: ngitb");
    }

    @Test
    void shouldFail_whenCaseFieldNotRegistered() {
        final FieldTypeEntity f = mock(FieldTypeEntity.class);
        parseContext.registerCaseFieldType("ngitb", "case_field", f);

        assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getCaseFieldType("ngitb", "case field"),
            "No types registered for case field ID: ngitb/case field");
    }

    @Test
    void shouldFail_whenCaseTypeNotRegisteredForState() {
        final CaseFieldEntity o = mock(CaseFieldEntity.class);
        given(o.getReference()).willReturn("CF");
        parseContext.registerCaseFieldForCaseType("ngitb", o);
        assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getStateForCaseType("ngitb", "state"),
            "No states registered for case type: ngitb");
    }

    @Test
    void shouldFail_whenStateNotRegistered() {
        final StateEntity s = mock(StateEntity.class);
        given(s.getReference()).willReturn("State");

        parseContext.registerStateForCaseType("ngitb", s);

        assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getStateForCaseType("ngitb", "state"),
            "No state registered for state ID: ngitb/state");
    }

    @Test
    void shouldFail_whenCaseTypeNotRegisteredFormCaseField() {
        assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.getCaseFieldForCaseType("ngitb", "state");
        }, "No case fields registered for case type: ngitb");
    }

    @Test
    void shouldFail_whenCaseFieldNotRegisteredForCaseType() {
        assertThrows(SpreadsheetParsingException.class, () -> {

            parseContext.getCaseFieldForCaseType("ngitb", "cf");

        }, "Unknown field cf for case type ngitb");
    }

    @Test
    void shouldGetEmpty_whenGetBaseTypeNotExist() {
        final Optional<FieldTypeEntity> type = parseContext.getBaseType("type");
        assertThat(type, isEmpty());
    }

    @Test
    void shouldAddAndGetBaseType() {
        final FieldTypeEntity t = mock(FieldTypeEntity.class);
        given(t.getReference()).willReturn("ngitb");

        parseContext.addBaseTypes(Arrays.asList(t));
        final Optional<FieldTypeEntity> type = parseContext.getBaseType("ngitb");
        assertThat(type.get(), is(t));
    }

    @Test
    void shouldAddToAllTypesAndGet() {
        final FieldTypeEntity t = mock(FieldTypeEntity.class);
        given(t.getReference()).willReturn("ngitb");

        parseContext.addToAllTypes(Arrays.asList(t));
        final Optional<FieldTypeEntity> type = parseContext.getType("ngitb");
        assertThat(type.get(), is(t));
    }

    @Test
    void shouldGetEmpty_whenGetTypeNotExist() {
        final Optional<FieldTypeEntity> type = parseContext.getType("ngitb");
        assertThat(type, isEmpty());
    }

    @Test
    void shouldReturnMetadataField_whenGetCaseFieldForCaseTypeIsMetadataField() {
        String metadataFieldName = "[METADATA]";
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("TEST");
        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        CaseFieldEntity metadataField = new CaseFieldEntity();
        metadataField.setReference(metadataFieldName);
        parseContext.registerMetadataFields(Collections.singletonList(metadataField));

        CaseFieldEntity response = parseContext.getCaseFieldForCaseType("caseTypeId", metadataFieldName);

        assertThat(response, is(metadataField));
    }

    @Test
    void testRegisterCaseTypeForCategory() {

        final CategoryEntity category = new CategoryEntity();
        category.setCategoryId("A");

        parseContext.registerCaseTypeForCategory("caseTypeId", category);

        assertEquals(parseContext.getCategory("caseTypeId", category.getCategoryId()), category);
    }

    @Test
    void testRegisterCaseTypeForCategoryWithSameIdTwiceShouldFail() {

        final CategoryEntity category = new CategoryEntity();
        category.setCategoryId("Category");

        parseContext.registerCaseTypeForCategory("caseTypeId", category);

        assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerCaseTypeForCategory("caseTypeId", category);
        }, "Category already registered for ID: Category");
    }

    @Test
    void testGetCategoryWhenEmpty() {
        assertNull(parseContext.getCategory("caseTypeId", "Category"));
    }

    @Test
    void testGetComplexTypes() {
        FieldTypeEntity fieldTypeEntity1 = new FieldTypeEntity();
        fieldTypeEntity1.setReference("Entity1");
        FieldTypeEntity complexEntity = new FieldTypeEntity();
        complexEntity.setReference("Complex");
        fieldTypeEntity1.setBaseFieldType(complexEntity);

        FieldTypeEntity fieldTypeEntity2 = new FieldTypeEntity();
        fieldTypeEntity2.setReference("Entity2");
        FieldTypeEntity notComplexEntity = new FieldTypeEntity();
        notComplexEntity.setReference("NotComplex");
        fieldTypeEntity2.setBaseFieldType(notComplexEntity);

        parseContext.addToAllTypes(fieldTypeEntity1);
        parseContext.addToAllTypes(fieldTypeEntity2);

        assertEquals(1, parseContext.getComplexTypes().size());
        assertTrue(parseContext.getComplexTypes().contains(fieldTypeEntity1));
    }

    @Test
    void testGetComplexTypesEmpty() {
        assertEquals(0, parseContext.getComplexTypes().size());
    }

    @Test
    void testIsCheckCategoryExists() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId("A");
        String caseTypeId = "CaseTypeId";
        parseContext.registerCaseTypeForCategory(caseTypeId, categoryEntity);
        assertTrue(parseContext.checkCategoryExists("A"));
    }

    @Test
    void testIsCheckCategoryDoesNotExists() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId("B");
        String caseTypeId = "CaseTypeId";
        parseContext.registerCaseTypeForCategory(caseTypeId, categoryEntity);
        assertFalse(parseContext.checkCategoryExists("A"));
    }
}
