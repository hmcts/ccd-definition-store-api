package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportValidationException;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
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
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ValidationResponseAssertions.assertImportValidationResponse;
import static uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ValidationResponseAssertions.assertValidationResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ParseContextTest {

    private ParseContext parseContext;

    @BeforeEach
    void setUp() {
        parseContext = new ParseContext();
    }

    @Test
    void missingCaseRolesRetainValidationFeedback() {
        var failure = assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getCaseRoleForCaseType("TestCase", "[MissingRole]"));
        assertImportValidationResponse(failure, "No case roles registered for case type: TestCase");
    }

    @Test
    void unknownCaseRolesRetainValidationFeedback() {
        var role = new CaseRoleEntity();
        role.setReference("[KnownRole]");
        parseContext.registerCaseRoleForCaseType("TestCase", role);
        var failure = assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getCaseRoleForCaseType("TestCase", "[MissingRole]"));
        assertImportValidationResponse(failure, "No case role registered for case role ID: TestCase/[MissingRole]");
    }

    @Test
    void duplicateEventsRetainValidationFeedback() {
        var event = new EventEntity();
        event.setReference("create");
        parseContext.registerEvent("TestCase", event);
        var failure = assertThrows(
            ImportValidationException.class,
            () -> parseContext.registerEvent("TestCase", event));
        assertEquals("Event already registered for ID: create", failure.getMessage());
        assertValidationResponse(failure);
    }

    @Test
    void shouldFail_whenRegisterCaseFieldTypeAgainForCaseType() {

        final CaseFieldEntity caseField = mock(CaseFieldEntity.class);
        given(caseField.getReference()).willReturn("case field id");

        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);
        });
        assertImportValidationResponse(failure, "Case field already registered for ID: case field id");
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

        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);
        });
        assertImportValidationResponse(failure, "Type already registered for field: fieldId");
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

        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerStateForCaseType("case type id", state);
        });
        assertImportValidationResponse(failure, "State already registered for ID: some state id");
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
        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.getCaseFieldType("ngitb", "case field");
        });
        assertImportValidationResponse(failure, "No types registered for case type: ngitb");
    }

    @Test
    void shouldFail_whenCaseFieldNotRegistered() {
        final FieldTypeEntity f = mock(FieldTypeEntity.class);
        parseContext.registerCaseFieldType("ngitb", "case_field", f);

        var failure = assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getCaseFieldType("ngitb", "case field"));
        assertImportValidationResponse(failure, "No types registered for case field ID: ngitb/case field");
    }

    @Test
    void shouldFail_whenCaseTypeNotRegisteredForState() {
        final CaseFieldEntity o = mock(CaseFieldEntity.class);
        given(o.getReference()).willReturn("CF");
        parseContext.registerCaseFieldForCaseType("ngitb", o);
        var failure = assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getStateForCaseType("ngitb", "state"));
        assertImportValidationResponse(failure, "No states registered for case type: ngitb");
    }

    @Test
    void shouldFail_whenStateNotRegistered() {
        final StateEntity s = mock(StateEntity.class);
        given(s.getReference()).willReturn("State");

        parseContext.registerStateForCaseType("ngitb", s);

        var failure = assertThrows(SpreadsheetParsingException.class,
            () -> parseContext.getStateForCaseType("ngitb", "state"));
        assertImportValidationResponse(failure, "No state registered for state ID: ngitb/state");
    }

    @Test
    void shouldFail_whenCaseTypeNotRegisteredFormCaseField() {
        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.getCaseFieldForCaseType("ngitb", "state");
        });
        assertImportValidationResponse(failure, "No case fields registered for case type: ngitb");
    }

    @Test
    void shouldFail_whenCaseFieldNotRegisteredForCaseType() {
        var existing = new CaseFieldEntity();
        existing.setReference("knownField");
        parseContext.registerCaseFieldForCaseType("ngitb", existing);
        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.getCaseFieldForCaseType("ngitb", "cf");
        });
        assertImportValidationResponse(failure, "Unknown field cf for case type ngitb");
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

        var failure = assertThrows(SpreadsheetParsingException.class, () -> {
            parseContext.registerCaseTypeForCategory("caseTypeId", category);
        });
        assertImportValidationResponse(failure, "Category already registered for ID: Category");
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
