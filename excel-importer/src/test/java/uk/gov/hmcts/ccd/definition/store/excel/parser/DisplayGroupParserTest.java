package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class DisplayGroupParserTest extends ParserTestBase {

    private CaseTypeTabParser caseTypeTabParser;
    private WizardPageParser wizardPageParser;
    private DefinitionSheet caseEventToFieldsSheet;
    private ShowConditionParser mockShowConditionParser;
    private EntityToDefinitionDataItemRegistry mockEntityToDefinitionRegistry;
    private static final ShowCondition PARSED_SHOW_CONDITION = new ShowCondition.Builder()
        .showConditionExpression("parsedShowCondition2").build();

    @BeforeEach
    void setup() {

        init();

        parseContext = mock(ParseContext.class);
        caseType = mock(CaseTypeEntity.class);
        mockShowConditionParser = mock(ShowConditionParser.class);
        mockEntityToDefinitionRegistry = mock(EntityToDefinitionDataItemRegistry.class);

        caseTypeTabParser = new CaseTypeTabParser(
            parseContext, mockShowConditionParser, mockEntityToDefinitionRegistry);
        wizardPageParser = new WizardPageParser(parseContext, mockShowConditionParser, mockEntityToDefinitionRegistry);

        definitionSheets.put(SheetName.CASE_TYPE_TAB.getName(), definitionSheet);
        caseEventToFieldsSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.CASE_EVENT_TO_FIELDS.getName(), caseEventToFieldsSheet);

        new DefinitionSheet();
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when worksheet missing")
    void shouldFail_whenSheetDoesNotExist() {
        MapperException thrown = assertThrows(MapperException.class, () -> caseTypeTabParser.parseAll(new HashMap<>()));
        assertThat(thrown.getMessage(), is(String.format(
            "A definition must contain a CaseTypeTab sheet with at least one entry",
            CASE_TYPE_UNDER_TEST)));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when at least one CaseField doesn't exist")
    void shouldFail_whenEmptyDisplayGroupDefinitionsIfDGItemIsMandatory() {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(), is(String.format(
            "At least one CaseField must be defined in the CaseTypeTab for case type %s",
            CASE_TYPE_UNDER_TEST)));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when page title missing")
    void shouldFail_whenMandatoryPageTitleNotGiven() {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);

        caseEventToFieldsSheet.addDataItem(item);
        MapperException thrown = assertThrows(MapperException.class, () -> wizardPageParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(), is("Couldn't find the column PageID in the sheet CaseEventToFields"));
    }

    @Test
    @DisplayName("WizardPageParser - should parse when everything is fine")
    void shouldParseCaseEventToFields() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .willReturn(PARSED_SHOW_CONDITION);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "someShowCondition");

        caseEventToFieldsSheet.addDataItem(item);
        final ParseResult<DisplayGroupEntity> parseResult = wizardPageParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults().size(), is(1));

        final DisplayGroupEntity fetched = parseResult.getAllResults().get(0);
        assertThat(fetched.getId(), is(nullValue()));
        assertThat(fetched.getReference(), is("SomeEvent" + "Name"));
        assertThat(fetched.getLabel(), is("Name"));
        assertThat(fetched.getType(), is(DisplayGroupType.PAGE));
        assertThat(fetched.getPurpose(), is(DisplayGroupPurpose.EDIT));
        assertThat(fetched.getOrder(), is(1));
        assertThat(fetched.getCaseType(), is(caseType));
        assertThat(fetched.getShowCondition(), is("parsedShowCondition2"));
    }

    @Test
    @DisplayName("WizardPageParser - should parse more than one item")
    void shouldParseCaseEventToFieldsEvenWithSamePageId() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .willReturn(PARSED_SHOW_CONDITION);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "someShowCondition");

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeOtherEvent");
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item2.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item2.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "someShowCondition");

        caseEventToFieldsSheet.addDataItem(item);
        caseEventToFieldsSheet.addDataItem(item2);

        final ParseResult<DisplayGroupEntity> parseResult = wizardPageParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults().size(), is(2));
    }

    @Test
    @DisplayName("WizardPageParser - should fail when more than one page show condition defined")
    void shouldFailIfTwoPageShowConditionsForSameEventPageID() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .willReturn(PARSED_SHOW_CONDITION);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "someShowCondition");

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "someShowCondition");

        caseEventToFieldsSheet.addDataItem(item);
        caseEventToFieldsSheet.addDataItem(item2);

        MapperException result = assertThrows(MapperException.class, () -> wizardPageParser.parseAll(definitionSheets));
        assertThat(result.getMessage(), equalTo("Please provide single condition in PageShowCondition column "
            + "in CaseEventToFields for the tab SomeEventName"));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when  when more than one tab show condition defined")
    void shouldFailIfTwoTabShowConditionsForSameTab() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Collections.singleton(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition"))
            .willReturn(PARSED_SHOW_CONDITION);
        given(mockShowConditionParser.parseShowCondition("fieldShowCondition"))
            .willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.TAB_ID.toString(), "Name");
        item.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), "someShowCondition");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "fieldShowCondition");

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.TAB_ID.toString(), "Name");
        item2.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), "someShowCondition");
        item2.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "fieldShowCondition");

        definitionSheet.addDataItem(item);
        definitionSheet.addDataItem(item2);

        MapperException result = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(result.getMessage(), equalTo("Please provide single condition in TabShowCondition column "
            + "in CaseTypeTab for the tab Name"));
    }

    @Test
    @DisplayName("WizardPageParser - should parse with data on any row")
    void shouldParsePageWithDataComingFromTheFirstRowAndIgnoreItsOtherRows()
        throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition2"))
            .willReturn(PARSED_SHOW_CONDITION);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_LABEL.toString(), "A Label");
        item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 3.0);
        item.addAttribute(ColumnName.PAGE_COLUMN.toString(), 2.0);

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item2.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item2.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_LABEL.toString(), "A different label");
        item2.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 2.0);
        item2.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "someShowCondition2");

        caseEventToFieldsSheet.addDataItem(item);
        caseEventToFieldsSheet.addDataItem(item2);

        final ParseResult<DisplayGroupEntity> parseResult = wizardPageParser.parseAll(definitionSheets);
        assertThat(parseResult.getAllResults().size(), is(1));

        final DisplayGroupEntity fetched = parseResult.getAllResults().get(0);
        assertThat(fetched.getId(), is(nullValue()));
        assertThat(fetched.getReference(), is("SomeEvent" + "Name"));
        assertThat(fetched.getLabel(), is("A Label"));
        assertThat(fetched.getType(), is(DisplayGroupType.PAGE));
        assertThat(fetched.getPurpose(), is(DisplayGroupPurpose.EDIT));
        assertThat(fetched.getOrder(), is(1));
        assertThat(fetched.getCaseType(), is(caseType));
        assertThat(fetched.getShowCondition(), is("parsedShowCondition2"));
        assertThat(fetched.getDisplayGroupCaseFields(), hasSize(2));
        DisplayGroupCaseFieldEntity cfe = fetched.getDisplayGroupCaseFields().iterator().next();
        assertThat(cfe.getDisplayGroup().getReference(), is("SomeEventName"));
        assertThat(cfe.getColumnNumber(), is(2));
        assertThat(cfe.getOrder(), is(3));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should parse CaseTypeTab")
    void shouldParseCaseTypeTab() throws InvalidShowConditionException {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());
        given(parseContext.getRole(CASE_TYPE_UNDER_TEST, "Role1")).willReturn(Optional.of(userRoleEntity));

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.USER_ROLE.toString(), "Role1");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");

        // Excel parses an integer into a decimal number
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);

        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        definitionSheet.addDataItem(item);

        final ParseResult<DisplayGroupEntity> parseResult = caseTypeTabParser.parseAll(definitionSheets);

        assertThat(parseResult.getAllResults().size(), is(1));

        final DisplayGroupEntity fetched = parseResult.getAllResults().get(0);
        assertThat(fetched.getId(), is(nullValue()));
        assertThat(fetched.getChannel(), is("CaseWorker"));
        assertThat(fetched.getLabel(), is("Name"));
        assertThat(fetched.getReference(), is("NameTab"));
        assertThat(fetched.getType(), is(DisplayGroupType.TAB));
        assertThat(fetched.getPurpose(), is(DisplayGroupPurpose.VIEW));
        assertThat(fetched.getOrder(), is(1));
        assertThat(fetched.getCaseType(), is(caseType));
        assertThat(fetched.getUserRole(), is(userRoleEntity));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when multiple user roles for same CaseTypeTab")
    void shouldNotParseCaseTypeTabForMultipleEntriesInUserRoles() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.USER_ROLE.toString(), "Role1");
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item2.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item2.addAttribute(ColumnName.USER_ROLE.toString(), "Role1");
        item2.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item2.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        definitionSheet.addDataItem(item2);

        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(), is("Please provide one user role row per tab in worksheet CaseTypeTab "
            + "on column USER_ROLE for the tab NameTab"));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when invalid user roles")
    void shouldNotParseCaseTypeTabForInvalidUserRoles() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(parseContext.getRole(CASE_TYPE_UNDER_TEST, "Role1")).willReturn(Optional.empty());
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.USER_ROLE.toString(), "Role1");
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        definitionSheet.addDataItem(item);

        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(),
            is("- Invalid idam or case role 'Role1' in 'CaseTypeTab' tab for TabId 'NameTab'"));
    }

    @Test
    @DisplayName("should fail for invalid CaseRole")
    void shouldFailForInvalidCaseRole() throws InvalidShowConditionException {
        final String caseRole = "[CLAIMANT]";
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(any())).willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.TAB_ID.toString(), "Name Tab");
        item1.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        definitionSheet.addDataItem(item1);
        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(),
            is("- Invalid idam or case role '[CLAIMANT]' in 'CaseTypeTab' tab for TabId 'Name Tab'"));
    }

    @Test
    @DisplayName("should parse for valid CaseRole")
    void shouldParseForValidCaseRole() throws InvalidShowConditionException {
        final String caseRole = "[CLAIMANT]";
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(any())).willReturn(new ShowCondition.Builder().build());
        given(parseContext.getRole(CASE_TYPE_UNDER_TEST, caseRole)).willReturn(Optional.of(caseRoleEntity));

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.TAB_ID.toString(), "Name Tab");
        item1.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        definitionSheet.addDataItem(item1);
        final ParseResult<DisplayGroupEntity> parseResult = caseTypeTabParser.parseAll(definitionSheets);
    }
}
