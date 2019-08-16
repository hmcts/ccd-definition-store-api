package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_RESULT_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_RESULT_FIELDS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

@DisplayName("Generic Layout Parser Test")
public class GenericLayoutParserTest {
    private static final String INVALID_CASE_TYPE_ID = "Invalid Case Type";
    private static final String ROLE1 = "Role1";
    private static final String INVALID_USER_ROLE = "Invalid User Role";
    private static final String CASE_TYPE_ID = "Valid Case Type";
    private static final String CASE_TYPE_ID2 = "Valid Case Type II";
    private static final String CASE_FIELD_ID_1 = "Field 1";
    private static final String CASE_FIELD_ID_2 = "Field 2";
    private static final String LIST_ELEMENT_CODE_1 = "Code1";
    private GenericLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private final ParseContext context = new ParseContext();

    @BeforeEach
    public void setup() {
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
        classUnderTest = new WorkbasketLayoutParser(context, entityToDefinitionDataItemRegistry);
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
    @DisplayName("Unknown user roles should generate error")
    public void shouldFailForInvalidUserRole() {
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
        item2.addAttribute(ColumnName.USER_ROLE, INVALID_USER_ROLE);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("- Unknown idam role '%s' in worksheet '%s' for caseField '%s'",
            INVALID_USER_ROLE, item2.getSheetName(), item2.getString(ColumnName.CASE_FIELD_ID)), thrown.getMessage());
    }

    @Test
    @DisplayName("Duplicate user roles should generate error")
    public void shouldFailForDuplicateUserRole() {
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
        item2.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item3.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item3);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setReference(ROLE1);
        context.registerUserRoles(Arrays.asList(userRoleEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerUserRoles(Arrays.asList(new UserRoleEntity()));
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
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setReference(ROLE1);
        context.registerUserRoles(Arrays.asList(userRoleEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerUserRoles(Arrays.asList(new UserRoleEntity()));
    }

    @Test
    @DisplayName("Duplicate user role and list element code definitions should generate error")
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
        item2.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item3.addAttribute(ColumnName.LIST_ELEMENT_CODE, LIST_ELEMENT_CODE_1);
        item3.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item3);

        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setReference(ROLE1);
        context.registerUserRoles(Arrays.asList(userRoleEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerUserRoles(Arrays.asList(new UserRoleEntity()));
    }

    @Test
    @DisplayName("Duplicate definitions without user role and list element code should generate error")
    public void shouldFailForDuplicateDefinitionItemsWithoutRoleAndListElementCodes() {
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
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setReference(ROLE1);
        context.registerUserRoles(Arrays.asList(userRoleEntity));
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Please make sure each row in worksheet %s is unique for case type %s",
            item3.getSheetName(), item3.getString(ColumnName.CASE_TYPE_ID)), thrown.getMessage());

        context.registerUserRoles(Arrays.asList(new UserRoleEntity()));
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
        assertEquals(String.format("Invalid results ordering pattern '%s' in worksheet '%s' for caseType '%s' for caseField '%s'",
            invalidSortOrder, item.getSheetName(), CASE_TYPE_ID, item.getString(ColumnName.CASE_FIELD_ID)), thrown.getMessage());
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
    @DisplayName("duplicate sort order priority with in the same user role should generate error")
    public void shouldFailForDuplicateSortOrderPriorityWithInUserRole() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        item.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "1:ASC");
        item2.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item2);
        addCaseType2Field(sheet); // CASE_TYPE_ID2
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parseAll(definitionSheets));
        assertEquals(String.format("Duplicate sort order priority in worksheet '%s' for caseType '%s'",
            item.getSheetName(), CASE_TYPE_ID), thrown.getMessage());
    }

    @Test
    @DisplayName("duplicate sort order priority per user role should generate error")
    public void shouldFailForDuplicateSortOrderPriorityPerUserRole() {
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
        item2.addAttribute(ColumnName.USER_ROLE, ROLE1);
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
    @DisplayName("Missing sort order priority for the same user role should generate error")
    public void shouldFailForGapsInSortOrderPriorityWithInUserRole() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_1);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        item.addAttribute(ColumnName.USER_ROLE, ROLE1);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        item2.addAttribute(ColumnName.RESULTS_ORDERING, "2:ASC");
        item2.addAttribute(ColumnName.USER_ROLE, ROLE1);
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

        classUnderTest = new SearchResultLayoutParser(context, entityToDefinitionDataItemRegistry);
        ParseResult<GenericLayoutEntity> parseResult = classUnderTest.parseAll(definitionSheets);

        assertEquals(parseResult.getAllResults().size(), 2);
        assertThat(parseResult.getAllResults(), hasItem(hasProperty("sortOrder",
            hasProperty("direction", equalTo("DESC")))));
        assertThat(parseResult.getAllResults(), hasItem(hasProperty("sortOrder",
            hasProperty("priority", equalTo(1)))));
    }

    private void addCaseType2Field(DefinitionSheet sheet) {
        final DefinitionDataItem item3 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID2);
        item3.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID_2);
        item3.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item3);
    }

}
