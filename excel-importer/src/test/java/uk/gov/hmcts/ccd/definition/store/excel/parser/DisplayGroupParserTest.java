package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DisplayGroupParserTest extends ParserTestBase {

    private CaseTypeTabParser caseTypeTabParser;
    private WizardPageParser wizardPageParser;
    private DefinitionSheet caseEventToFieldsSheet;
    private ShowConditionParser mockShowConditionParser;
    private EntityToDefinitionDataItemRegistry mockEntityToDefinitionRegistry;
    private static final ShowCondition PARSED_SHOW_CONDITION = new ShowCondition.Builder()
        .showConditionExpression("parsedShowCondition2").build();
    private static final String ACCESS_PROFILE = "AccessProfile1";
    public static final String SOME_SHOW_CONDITION = "someShowCondition";

    @BeforeEach
    void setUp() {

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
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), false);

        caseEventToFieldsSheet.addDataItem(item);
        MapperException thrown = assertThrows(MapperException.class, () -> wizardPageParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(), is("Couldn't find the column PageID in the sheet CaseEventToFields"));
    }

    @Test
    @DisplayName("WizardPageParser - should parse when everything is fine")
    void shouldParseCaseEventToFields() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(SOME_SHOW_CONDITION))
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
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);

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
    @DisplayName("WizardPageParser - should parse when midpoint callback url is not mentioned in first line")
    void shouldParseCaseEventToFieldsWhenMidPointCallBackURL() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(SOME_SHOW_CONDITION))
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
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);

        caseEventToFieldsSheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item2.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 2.0);
        item2.addAttribute(ColumnName.CALLBACK_URL_MID_EVENT.toString(),
            "http://host.docker.internal:8080/ccdMidEvent?pageId=bla");


        caseEventToFieldsSheet.addDataItem(item2);
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
        assertThat(fetched.getWebhookMidEvent().getUrl(), is("http://host.docker.internal:8080/ccdMidEvent?pageId=bla"));

    }

    @Test
    @DisplayName("WizardPageParser - should parse more than one item")
    void shouldParseCaseEventToFieldsEvenWithSamePageId() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(SOME_SHOW_CONDITION))
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
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeOtherEvent");
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item2.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item2.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);

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
        given(mockShowConditionParser.parseShowCondition(SOME_SHOW_CONDITION))
            .willReturn(PARSED_SHOW_CONDITION);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.PAGE_ID.toString(), "Name");
        item2.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);

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
        given(mockShowConditionParser.parseShowCondition(SOME_SHOW_CONDITION))
            .willReturn(PARSED_SHOW_CONDITION);
        given(mockShowConditionParser.parseShowCondition("fieldShowCondition"))
            .willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.TAB_ID.toString(), "Name");
        item.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "fieldShowCondition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "Y");

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.TAB_ID.toString(), "Name");
        item2.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);
        item2.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "fieldShowCondition");
        item2.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "N");

        definitionSheet.addDataItem(item);
        definitionSheet.addDataItem(item2);

        MapperException result = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(result.getMessage(), equalTo("Please provide single condition in TabShowCondition column "
            + "in CaseTypeTab for the tab Name"));
    }


    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("CaseTypeTabParser - should fail when multiple defaultFocus condition defined for caseType")
    void shouldFailIfTwoDefaultFocusValuesSetForSameCaseType(boolean firstItemHasDefaultFocus)
        throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Collections.singleton(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(SOME_SHOW_CONDITION))
            .willReturn(PARSED_SHOW_CONDITION);
        given(mockShowConditionParser.parseShowCondition("fieldShowCondition"))
            .willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.TAB_ID.toString(), "Name");
        item.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "fieldShowCondition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), firstItemHasDefaultFocus);

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "Age");
        item2.addAttribute(ColumnName.TAB_ID.toString(), "Details");
        item2.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);
        item2.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), !firstItemHasDefaultFocus);

        final DefinitionDataItem item3 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item3.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item3.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "Age");
        item3.addAttribute(ColumnName.TAB_ID.toString(), "Details");
        item3.addAttribute(ColumnName.TAB_SHOW_CONDITION.toString(), SOME_SHOW_CONDITION);
        item3.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), true);

        definitionSheet.addDataItem(item);
        definitionSheet.addDataItem(item2);
        definitionSheet.addDataItem(item3);

        ImportValidationException result = assertThrows(ImportValidationException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(result.getMessage(),
            equalTo("Only one row per case type can have DefaultFocus set to true in worksheet CaseTypeTab; "
                + "found 2 for case type " + CASE_TYPE_UNDER_TEST));
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
        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());
        given(parseContext.getAccessProfile(CASE_TYPE_UNDER_TEST, ACCESS_PROFILE))
            .willReturn(Optional.of(accessProfileEntity));

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "Y");

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
        assertThat(fetched.getAccessProfile(), is(accessProfileEntity));
        assertThat(fetched.getDefaultFocus(), is(true));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should set defaultFocus when it is set on a row other than the first")
    void shouldParseDefaultFocusSetOnNonFirstRowOfTab() throws InvalidShowConditionException {
        givenCaseTypeTabContext();
        definitionSheet.addDataItem(caseTypeTabItem("PersonFirstName", null));
        definitionSheet.addDataItem(caseTypeTabItem("PersonLastName", "Y"));

        final ParseResult<DisplayGroupEntity> parseResult = caseTypeTabParser.parseAll(definitionSheets);

        assertThat(parseResult.getAllResults().size(), is(1));
        assertThat(parseResult.getAllResults().get(0).getDefaultFocus(), is(true));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should leave defaultFocus null when the DefaultFocus column is absent")
    void shouldLeaveDefaultFocusNullWhenColumnAbsent() throws InvalidShowConditionException {
        givenCaseTypeTabContext();
        definitionSheet.addDataItem(caseTypeTabItem("PersonFirstName", null));
        definitionSheet.addDataItem(caseTypeTabItem("PersonLastName", null));

        final ParseResult<DisplayGroupEntity> parseResult = caseTypeTabParser.parseAll(definitionSheets);

        assertThat(parseResult.getAllResults().get(0).getDefaultFocus(), is(nullValue()));
    }

    private void givenCaseTypeTabContext() throws InvalidShowConditionException {
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());
        given(parseContext.getAccessProfile(CASE_TYPE_UNDER_TEST, ACCESS_PROFILE))
            .willReturn(Optional.of(new AccessProfileEntity()));
    }

    private DefinitionDataItem caseTypeTabItem(String caseFieldId, String defaultFocus) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        if (definitionSheet.getDataItems().isEmpty()) {
            // only one row per tab may carry the access profile
            item.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        }
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), caseFieldId);
        if (defaultFocus != null) {
            item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), defaultFocus);
        }
        return item;
    }

    @Test
    @DisplayName("CaseTypeTabParser - should parse CaseTypeTab fail with two defaultFocus for same caseType")
    void shouldFailParseCaseTypeTabTwoDefaultFocus() throws InvalidShowConditionException {
        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());
        given(parseContext.getAccessProfile(CASE_TYPE_UNDER_TEST, ACCESS_PROFILE))
            .willReturn(Optional.of(accessProfileEntity));

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "Y");
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item2.addAttribute(ColumnName.TAB_ID.toString(), "Details");
        item2.addAttribute(ColumnName.TAB_LABEL.toString(), "Details");
        item2.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        item2.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), true);
        item2.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 2.0);
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "Age");

        definitionSheet.addDataItem(item);
        definitionSheet.addDataItem(item2);

        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(),
            is("Only one row per case type can have DefaultFocus set to true in worksheet CaseTypeTab; "
                + "found 2 for case type " + CASE_TYPE_UNDER_TEST));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when multiple access profiles for same CaseTypeTab")
    void shouldNotParseCaseTypeTabForMultipleEntriesInAccessProfiles() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "Y");
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item2.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item2.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        item2.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item2.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item2.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonLastName");
        item2.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "yes");
        definitionSheet.addDataItem(item2);

        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(), is("Please provide one access profile row per tab in worksheet "
            + "CaseTypeTab on column ACCESS_PROFILE for the tab NameTab"));
    }

    @Test
    @DisplayName("CaseTypeTabParser - should fail when invalid access profiles")
    void shouldNotParseCaseTypeTabForInvalidAccessProfiles() throws InvalidShowConditionException {
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(parseContext.getAccessProfile(CASE_TYPE_UNDER_TEST, ACCESS_PROFILE)).willReturn(Optional.empty());
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(anyString())).willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item.addAttribute(ColumnName.ACCESS_PROFILE.toString(), ACCESS_PROFILE);
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "show condition");
        item.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "yes");
        definitionSheet.addDataItem(item);

        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(),
            is("- Invalid access profile or case role 'AccessProfile1' in 'CaseTypeTab' tab "
                + "for TabId 'NameTab'"));
    }

    @Test
    @DisplayName("should fail for invalid CaseRole")
    void shouldFailForInvalidCaseRole() throws InvalidShowConditionException {
        final String caseRole = "[CLAIMANT]";

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(any())).willReturn(new ShowCondition.Builder().build());

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.TAB_ID.toString(), "Name Tab");
        item1.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), caseRole);
        item1.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item1.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "yes");
        definitionSheet.addDataItem(item1);
        MapperException thrown = assertThrows(MapperException.class,
            () -> caseTypeTabParser.parseAll(definitionSheets));
        assertThat(thrown.getMessage(),
            is("- Invalid access profile or case role '[CLAIMANT]' in 'CaseTypeTab' tab "
                + "for TabId 'Name Tab'"));
    }

    @Test
    @DisplayName("should parse for valid CaseRole")
    void shouldParseForValidCaseRole() throws InvalidShowConditionException {
        final String caseRole = "[CLAIMANT]";
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition(any())).willReturn(new ShowCondition.Builder().build());
        given(parseContext.getAccessProfile(CASE_TYPE_UNDER_TEST, caseRole)).willReturn(Optional.of(caseRoleEntity));

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.TAB_ID.toString(), "Name Tab");
        item1.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), caseRole);
        item1.addAttribute(ColumnName.TAB_DISPLAY_ORDER.toString(), 1.0);
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
        item1.addAttribute(ColumnName.DEFAULT_FOCUS.toString(), "yes");
        definitionSheet.addDataItem(item1);
        final ParseResult<DisplayGroupEntity> parseResult = caseTypeTabParser.parseAll(definitionSheets);

        assertThat(parseResult.getAllResults().size(), is(1));

        final DisplayGroupEntity fetched = parseResult.getAllResults().get(0);
        assertThat(fetched.getAccessProfile(), is(caseRoleEntity));
    }
}
