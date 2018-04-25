package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
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

    @Before
    public void setup() {

        init();

        parseContext = mock(ParseContext.class);
        caseType = mock(CaseTypeEntity.class);
        mockShowConditionParser = mock(ShowConditionParser.class);
        mockEntityToDefinitionRegistry = mock(EntityToDefinitionDataItemRegistry.class);

        caseTypeTabParser = new CaseTypeTabParser(parseContext, mockShowConditionParser, mockEntityToDefinitionRegistry);
        wizardPageParser = new WizardPageParser(parseContext, mockShowConditionParser, mockEntityToDefinitionRegistry);

        definitionSheets.put(SheetName.CASE_TYPE_TAB.getName(), definitionSheet);
        caseEventToFieldsSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.CASE_EVENT_TO_FIELDS.getName(), caseEventToFieldsSheet);

        new DefinitionSheet();
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenSheetDoesNotExist() {

        try {
            caseTypeTabParser.parseAll(new HashMap<>());
        } catch (MapperException ex) {
            Assertions.assertThat(ex).hasMessageContaining(
                String.format("A definition must contain a CaseTypeTab sheet with at least one entry",
                    CASE_TYPE_UNDER_TEST));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenEmptyDisplayGroupDefinitionsIfDGItemIsMandatory() {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        try {
            caseTypeTabParser.parseAll(definitionSheets);
        } catch (MapperException ex) {
            Assertions.assertThat(ex).hasMessageContaining(
                String.format("At least one CaseField must be defined in the CaseTypeTab for case type %s",
                CASE_TYPE_UNDER_TEST));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenMandatoryPageTitleNotGiven() {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        try {
            final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
            item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
            item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), "SomeEvent");
            item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "PersonFirstName");
            item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
            item.addAttribute(ColumnName.PAGE_LABEL.toString(), "Name");
            item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
            item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);

            caseEventToFieldsSheet.addDataItem(item);
            final ParseResult<DisplayGroupEntity> parseResult = wizardPageParser.parseAll(definitionSheets);
        } catch (MapperException ex) {
            Assertions.assertThat(ex).hasMessageContaining(
                "Couldn't find the column PageID in the sheet CaseEventToFields");
            throw ex;
        }
    }

    @Test
    public void shouldParseCaseEventToFields() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition")).willReturn(PARSED_SHOW_CONDITION);

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
    public void shouldParseCaseEventToFieldsEvenWithSamePageId() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition")).willReturn(PARSED_SHOW_CONDITION);

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
    public void shouldParsePageWithDataComingFromTheFirstRowAndIgnoreItsOtherRows() throws InvalidShowConditionException {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);
        given(mockShowConditionParser.parseShowCondition("someShowCondition2")).willReturn(PARSED_SHOW_CONDITION);

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
    public void shouldParseCaseTypeTab() {

        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CHANNEL.toString(), "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID.toString(), "NameTab");
        item.addAttribute(ColumnName.TAB_LABEL.toString(), "Name");

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
    }
}
