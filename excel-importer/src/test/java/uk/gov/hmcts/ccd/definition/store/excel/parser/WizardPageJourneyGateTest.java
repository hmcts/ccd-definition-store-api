package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseResult;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class WizardPageJourneyGateTest extends ParserTestBase {

    private WizardPageParser wizardPageParser;
    private DefinitionSheet caseEventToFieldsSheet;
    private ShowConditionParser mockShowConditionParser;
    private EntityToDefinitionDataItemRegistry mockEntityToDefinitionRegistry;

    @BeforeEach
    void setUp() {
        init();
        parseContext = mock(ParseContext.class);
        caseType = mock(uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity.class);
        mockShowConditionParser = mock(ShowConditionParser.class);
        mockEntityToDefinitionRegistry = mock(EntityToDefinitionDataItemRegistry.class);

        wizardPageParser = new WizardPageParser(parseContext, mockShowConditionParser, mockEntityToDefinitionRegistry);

        definitionSheets.put(SheetName.CASE_TYPE_TAB.getName(), definitionSheet);
        caseEventToFieldsSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.CASE_EVENT_TO_FIELDS.getName(), caseEventToFieldsSheet);
    }

    @Test
    @DisplayName("should allow first page gate based on existing case data (metadata)")
    void shouldParseFirstPageShowConditionBasedOnMetadata() throws InvalidShowConditionException {
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(java.util.Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        String metadataField = MetadataField.STATE.getReference();
        given(mockShowConditionParser.parseShowCondition("[STATE]=\"Open\""))
            .willReturn(new ShowCondition.Builder()
                .showConditionExpression("parsedStateGate")
                .field(metadataField)
                .build());

        DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Start");
        item.addAttribute(ColumnName.PAGE_LABEL.toString(), "Start");
        item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "[STATE]=\"Open\"");
        caseEventToFieldsSheet.addDataItem(item);

        ParseResult<DisplayGroupEntity> parseResult = wizardPageParser.parseAll(definitionSheets);

        assertThat(parseResult.getAllResults().size(), is(1));
        DisplayGroupEntity page = parseResult.getAllResults().get(0);
        assertThat(page.getType(), is(DisplayGroupType.PAGE));
        assertThat(page.getPurpose(), is(DisplayGroupPurpose.EDIT));
        assertThat(page.getOrder(), is(1));
        assertThat(page.getLabel(), is("Start"));
        assertThat(page.getShowCondition(), is("parsedStateGate"));
        assertThat(page.getId(), is(nullValue()));
    }

    @Test
    @DisplayName("should parse later page gate based on data collected on an earlier page")
    void shouldParseLaterPageShowConditionBasedOnCollectedField() throws InvalidShowConditionException {
        given(parseContext.getCaseTypes()).willReturn(new HashSet<>(java.util.Arrays.asList(caseType)));
        given(caseType.getReference()).willReturn(CASE_TYPE_UNDER_TEST);

        given(mockShowConditionParser.parseShowCondition("Step1Field=\"Yes\""))
            .willReturn(new ShowCondition.Builder()
                .showConditionExpression("parsedCollectedGate")
                .field("Step1Field")
                .build());

        DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), CASE_EVENT_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "Step1Field");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.PAGE_ID.toString(), "Eligibility");
        item.addAttribute(ColumnName.PAGE_LABEL.toString(), "Eligibility");
        item.addAttribute(ColumnName.PAGE_DISPLAY_ORDER.toString(), 2.0);
        item.addAttribute(ColumnName.PAGE_FIELD_DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.PAGE_SHOW_CONDITION.toString(), "Step1Field=\"Yes\"");
        caseEventToFieldsSheet.addDataItem(item);

        ParseResult<DisplayGroupEntity> parseResult = wizardPageParser.parseAll(definitionSheets);

        assertThat(parseResult.getAllResults().size(), is(1));
        DisplayGroupEntity page = parseResult.getAllResults().get(0);
        assertThat(page.getOrder(), equalTo(2));
        assertThat(page.getShowCondition(), is("parsedCollectedGate"));
        assertThat(page.getReference(), equalTo(CASE_EVENT_UNDER_TEST + "Eligibility"));
    }
}
