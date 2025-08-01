package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_CASES_RESULT_FIELDS;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_INPUT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_RESULT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_RESULT_FIELDS;

@DisplayName("Generic Layout Parser Test")
class GenericLayoutParserTest {
    private static final String INVALID_CASE_TYPE_ID = "Invalid Case Type";
    private static final String ACCESS_PROFILE_1 = "AccessProfile1";
    private static final String INVALID_ACCESS_PROFILE = "Invalid Access Profile";
    private static final String CASE_TYPE_ID = "Valid Case Type";
    private static final String CASE_TYPE_ID2 = "Valid Case Type II";
    private static final String CASE_FIELD_ID_1 = "Field 1";
    private static final String CASE_FIELD_ID_2 = "Field 2";
    private static final String LIST_ELEMENT_CODE_1 = "Code1";
    private static final String PARSED_SHOW_CONDITION = "Parsed Show Condition";
    private static final String DISPLAY_CONTEXT_PARAMETER = "Display Context Parameter";
    private GenericLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private final ParseContext context = new ParseContext();

    @Mock
    private ShowConditionParser showConditionParser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();
        caseTypeEntity2.setReference(CASE_TYPE_ID2);
        CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();
        caseFieldEntity1.setReference(CASE_FIELD_ID_1);
        CaseFieldEntity caseFieldEntity2 = new CaseFieldEntity();
        caseFieldEntity2.setReference(CASE_FIELD_ID_2);


        context.registerCaseType(caseTypeEntity);
        context.registerCaseType(caseTypeEntity2);
        context.registerCaseFieldForCaseType(CASE_TYPE_ID, caseFieldEntity1);
        context.registerCaseFieldForCaseType(CASE_TYPE_ID2, caseFieldEntity2);

        definitionSheets = new HashMap<>();

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        classUnderTest = new WorkbasketLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);
    }

    @Test
    @DisplayName("Unknown definitions should generate error")
    public void shouldFailIfUnknownCaseType() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, INVALID_CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Unknown Case Type '%s' for layout '%s'",
            INVALID_CASE_TYPE_ID, classUnderTest.getLayoutName()), thrown.getMessage());
    }

    @Test
    @DisplayName("Missing workbasket result caseField definitions for caseTypes should generate error")
    public void shouldFailIfCaseTypeHasNoDefinition() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("At least one layout case field must be defined for case type %s and layout %s",
            CASE_TYPE_ID2, classUnderTest.getLayoutName()), thrown.getMessage());
    }

    @Test
    @DisplayName("Unknown access profiles should generate error")
    public void shouldFailForInvalidAccessProfile() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.ACCESS_PROFILE, INVALID_ACCESS_PROFILE);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("- Unknown access profile '%s' in worksheet '%s' for caseField '%s'",
            INVALID_ACCESS_PROFILE, item2.getSheetName(), item2.getString(ColumnName.CASE_FIELD_ID)),
            thrown.getMessage());
    }

    @Test
    @DisplayName("Duplicate access profiles should generate error")
    public void shouldFailForDuplicateAccessProfile() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item3.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item3);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_1);
        context.registerAccessProfiles(Arrays.asList(accessProfileEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerAccessProfiles(Arrays.asList(new AccessProfileEntity()));
    }

    @Test
    @DisplayName("Duplicate list element codes should generate error")
    public void shouldFailForDuplicateListElementCodes() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item3.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        sheet.addDataItem(item3);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_1);
        context.registerAccessProfiles(Arrays.asList(accessProfileEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerAccessProfiles(Arrays.asList(new AccessProfileEntity()));
    }

    @Test
    @DisplayName("Duplicate access profile and list element code definitions should generate error")
    public void shouldFailForDuplicateDefinitionItems() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item3.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item3.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item3);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_1);
        context.registerAccessProfiles(Arrays.asList(accessProfileEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerAccessProfiles(Arrays.asList(new AccessProfileEntity()));
    }

    @Test
    @DisplayName("Duplicate definitions without access profile and list element code should generate error")
    public void shouldFailForDuplicateDefinitionItemsWithoutAccessProfileAndListElementCodes() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        addCaseType2Field(sheet);
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item3);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_1);
        context.registerAccessProfiles(Arrays.asList(accessProfileEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerAccessProfiles(Arrays.asList(new AccessProfileEntity()));
    }

    @Test
    @DisplayName("Invalid sort order pattern should generate error")
    public void shouldFailForInvalidResultsOrderingPattern() {
        String invalidSortOrder = "2-ASCENDING";
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, invalidSortOrder);
        sheet.addDataItem(item);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format(
            "Invalid results ordering pattern '%s' in worksheet '%s' for caseType '%s' for caseField '%s'",
            invalidSortOrder, item.getSheetName(), CASE_TYPE_ID,
            item.getString(ColumnName.CASE_FIELD_ID)), thrown.getMessage());
    }

    @Test
    @DisplayName("duplicate sort order priority should generate error")
    public void shouldFailForDuplicateSortOrderPriority() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Duplicate sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("duplicate sort order priority with in the same access profile should generate error")
    public void shouldFailForDuplicateSortOrderPriorityWithInAccessProfile() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        item.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Duplicate sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("duplicate sort order priority per access profile should generate error")
    public void shouldFailForDuplicateSortOrderPriorityPerAccessProfile() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Duplicate sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("Missing sort order priority should generate error")
    public void shouldFailForGapsInSortOrderPriority() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "2:ASC");
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Missing sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("Missing sort order priority for the same access profile should generate error")
    public void shouldFailForGapsInSortOrderPriorityWithInAccessProfile() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "2:ASC");
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Missing sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("Should set sort order values for workbasket results")
    public void shouldSetResultsOrderingForWorkbasketResults() {
        String sortOrder = "1:ASC";
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, sortOrder);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);

        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAll(definitionSheets);

        assertEquals(parseResult.getAllResults().size(), 2);
        assertThat(parseResult.getAllResults(), hasItem(hasProperty("sortOrder",
            hasProperty("direction", equalTo("ASC")))));
        assertThat(parseResult.getAllResults(), hasItem(hasProperty("sortOrder",
            hasProperty("priority", equalTo(1)))));
    }

    @Test
    @DisplayName("Should set sort order values for search results")
    public void shouldSetResultsOrderingForSearchResults() {
        String sortOrder = "1:DESC";
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(SEARCH_RESULT_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, sortOrder);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(SEARCH_RESULT_FIELD.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item2);
        definitionSheets.put(SEARCH_RESULT_FIELD.getName(), sheet);

        classUnderTest = new SearchResultLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);
        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAll(definitionSheets);

        assertEquals(parseResult.getAllResults().size(), 2);
        assertThat(parseResult.getAllResults(), hasItem(hasProperty("sortOrder",
            hasProperty("direction", equalTo("DESC")))));
        assertThat(parseResult.getAllResults(), hasItem(hasProperty("sortOrder",
            hasProperty("priority", equalTo(1)))));
    }

    @Test
    @DisplayName("Should set show condition value for search input")
    public void shouldSetShowConditionForSearchInputs() throws InvalidShowConditionException {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(SEARCH_INPUT_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "some show condition");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(SEARCH_INPUT_FIELD.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        sheet.addDataItem(item2);

        definitionSheets.put(SEARCH_INPUT_FIELD.getName(), sheet);

        when(showConditionParser.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(PARSED_SHOW_CONDITION).build()
        );

        classUnderTest = new SearchInputLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);
        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAll(definitionSheets);

        assertEquals(parseResult.getAllResults().size(), 2);
        assertThat(parseResult.getAllResults(), hasItem(hasProperty(
            "showCondition", equalTo(PARSED_SHOW_CONDITION))));
    }

    @Test
    @DisplayName("Should set show condition value for workbasket input")
    public void shouldSetShowConditionForWorkbasketInputs() throws InvalidShowConditionException {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_INPUT_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "some show condition");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_INPUT_FIELD.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        sheet.addDataItem(item2);

        definitionSheets.put(WORK_BASKET_INPUT_FIELD.getName(), sheet);

        when(showConditionParser.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(PARSED_SHOW_CONDITION).build()
        );

        classUnderTest = new WorkbasketInputLayoutParser(
            context, entityToDefinitionDataItemRegistry, showConditionParser);
        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAll(definitionSheets);

        assertEquals(parseResult.getAllResults().size(), 2);
        assertThat(parseResult.getAllResults(), hasItem(hasProperty(
            "showCondition", equalTo(PARSED_SHOW_CONDITION))));
    }

    @DisplayName("Should fail when show condition appears in search results")
    @Test
    void shouldThrowExceptionIfShowConditionExistsInSearchResults() throws InvalidShowConditionException {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(SEARCH_RESULT_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "some show condition");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(SEARCH_RESULT_FIELD.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        sheet.addDataItem(item2);
        definitionSheets.put(SEARCH_RESULT_FIELD.getName(), sheet);

        when(showConditionParser.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(PARSED_SHOW_CONDITION).build()
        );

        classUnderTest = new SearchResultLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);

        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("showCondition is not supported in worksheet '%s' for caseType '%s'",
            SheetName.SEARCH_RESULT_FIELD.getName(), CASE_TYPE_ID), thrown.getMessage());

    }

    @DisplayName("Should fail when show condition appears in workbasket results")
    @Test
    void shouldThrowExceptionIfShowConditionExistsInWorkbasketResults() throws InvalidShowConditionException {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION, "some show condition");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);

        when(showConditionParser.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(PARSED_SHOW_CONDITION).build()
        );

        classUnderTest = new WorkbasketLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);

        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("showCondition is not supported in worksheet '%s' for caseType '%s'",
            SheetName.WORK_BASKET_RESULT_FIELDS.getName(), CASE_TYPE_ID), thrown.getMessage());

    }

    @Test
    @DisplayName("Should create a generic layout entity")
    public void shouldCreateGenericLayoutEntity() {
        ParseContext context = new ParseContext();
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_ID);
        CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();
        caseFieldEntity1.setReference(CASE_FIELD_ID_1);
        context.registerCaseType(caseTypeEntity);
        context.registerCaseFieldForCaseType(CASE_TYPE_ID, caseFieldEntity1);

        final String label = "LABEL";
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(SEARCH_RESULT_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:DESC");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, DISPLAY_CONTEXT_PARAMETER);
        item.addAttribute(ColumnName.LABEL, label);
        sheet.addDataItem(item);
        definitionSheets.put(SEARCH_RESULT_FIELD.getName(), sheet);

        classUnderTest = new SearchResultLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);
        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAll(definitionSheets);

        GenericLayoutEntity entity = parseResult.getAllResults().get(0);
        assertEquals(CASE_TYPE_ID, entity.getCaseType().getReference());
        assertEquals(CASE_FIELD_ID_1, entity.getCaseField().getReference());
        assertEquals((Integer) 3, entity.getOrder());
        assertEquals(label, entity.getLabel());
        assertEquals(DISPLAY_CONTEXT_PARAMETER, entity.getDisplayContextParameter());
    }

    @Test
    @DisplayName("Should create a generic layout entity")
    void shouldCreateGenericLayoutEntityParseAllSearchCases() {
        ParseContext context = new ParseContext();
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_ID);
        CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();
        caseFieldEntity1.setReference(CASE_FIELD_ID_1);
        context.registerCaseType(caseTypeEntity);
        context.registerCaseFieldForCaseType(CASE_TYPE_ID, caseFieldEntity1);

        final String label = "LABEL";
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(SEARCH_RESULT_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:DESC");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, DISPLAY_CONTEXT_PARAMETER);
        item.addAttribute(ColumnName.LABEL, label);
        sheet.addDataItem(item);
        definitionSheets.put(SEARCH_RESULT_FIELD.getName(), sheet);

        classUnderTest = new SearchResultLayoutParser(context, entityToDefinitionDataItemRegistry, showConditionParser);
        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAllForSearchCases(definitionSheets);

        GenericLayoutEntity entity = parseResult.getAllResults().get(0);
        assertEquals(CASE_TYPE_ID, entity.getCaseType().getReference());
        assertEquals(CASE_FIELD_ID_1, entity.getCaseField().getReference());
        assertEquals((Integer) 3, entity.getOrder());
        assertEquals(label, entity.getLabel());
        assertEquals(DISPLAY_CONTEXT_PARAMETER, entity.getDisplayContextParameter());
    }

    @Test
    @DisplayName("duplicate sort order priority per access profile should generate error")
    void shouldFailForDuplicateSortOrderPriorityPerAccessProfileParseAllSearchCases() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class,
            () -> classUnderTest.parseAllForSearchCases(definitionSheets));
        assertEquals(String.format("Duplicate sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("Unknown definitions should generate error")
    void shouldFailIfUnknownCaseTypeParseAllSearchCases() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, INVALID_CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class,
            () -> classUnderTest.parseAllForSearchCases(definitionSheets));
        assertEquals(String.format("Unknown Case Type '%s' for layout '%s'",
            INVALID_CASE_TYPE_ID, classUnderTest.getLayoutName()), thrown.getMessage());
    }

    @Test
    @DisplayName("Duplicate access profile and list element code definitions should generate error")
    void shouldFailForDuplicateDefinitionItemsForSearchCases() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(SEARCH_CASES_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        item.addAttribute(ColumnName.USE_CASE, "WORKBASKET");
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(SEARCH_CASES_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item2.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        item2.addAttribute(ColumnName.USE_CASE, "ORGCASES");
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(SEARCH_CASES_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item3.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item3.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        item3.addAttribute(ColumnName.USE_CASE, "ORGCASES");
        sheet.addDataItem(item3);

        definitionSheets.put(SEARCH_CASES_RESULT_FIELDS.getName(), sheet);

        final DefinitionSheet sheet1 = new DefinitionSheet();
        final DefinitionDataItem item1 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item1.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item1.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet1.addDataItem(item1);
        final DefinitionDataItem item4 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item4.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item4.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item4.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item4.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item4.addAttribute(ColumnName.ACCESS_PROFILE, ACCESS_PROFILE_1);
        sheet1.addDataItem(item4);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);

        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_1);
        context.registerAccessProfiles(Arrays.asList(accessProfileEntity));
        MapperException thrown = assertThrows(MapperException.class,
            () -> classUnderTest.parseAllForSearchCases(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerAccessProfiles(Arrays.asList(new AccessProfileEntity()));
    }

    private void addCaseType2Field(DefinitionSheet sheet) {
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item3);
    }

}
