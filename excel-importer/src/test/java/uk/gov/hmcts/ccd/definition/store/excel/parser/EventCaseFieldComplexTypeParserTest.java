package uk.gov.hmcts.ccd.definition.store.excel.parser;


import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DisplayContextColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventCaseFieldComplexTypeParserTest {
    private static final String PARSED_SHOW_CONDITION = "Parsed Show Condition";
    private static final String REFERENCE_ID = "test.test";
    private static final LocalDate LIVE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate LIVE_TO = LocalDate.of(2099, 1, 1);
    protected DefinitionSheet definitionSheet;

    @Mock
    private ShowConditionParser showConditionParser;

    @Mock
    private HiddenFieldsValidator hiddenFieldsValidator;

    @InjectMocks
    private EventCaseFieldComplexTypeParser eventCaseFieldComplexTypeParser;

    @BeforeEach
    void setUp() throws InvalidShowConditionException {
        MockitoAnnotations.openMocks(this);
        when(showConditionParser.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(PARSED_SHOW_CONDITION).build());
    }

    @Test
    void shouldParseEventCaseFieldComplexTypes() {
        String caseFieldId = "Case Field Id";
        String originalShowCondition = "Original Show Condition";
        String label = "label";
        String hint = "hint";
        String displayContextParameter = "Display Context Parameter";
        String publishAsTest = "publishAsTest";
        DisplayContextColumn displayContext = new DisplayContextColumn("OPTIONAL", DisplayContext.OPTIONAL);

        DefinitionDataItem definitionDataItem = definitionDataItem(caseFieldId, displayContext, originalShowCondition,
                                                                   label, hint, displayContextParameter, true,
                                                                   publishAsTest);

        List<DefinitionDataItem> definitionDataItems = singletonList(definitionDataItem);
        Map<String, DefinitionSheet> definitionSheets = new LinkedHashMap<>();;
        definitionSheets.put(SheetName.CASE_EVENT.getName(), definitionSheet);
        when(hiddenFieldsValidator
            .parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets)).thenReturn(false);
        List<EventComplexTypeEntity> eventComplexTypeEntities =
            eventCaseFieldComplexTypeParser.parseEventCaseFieldComplexType(definitionDataItems, null);

        assertEquals(1, eventComplexTypeEntities.size());
        assertEquals(definitionDataItem.getString(ColumnName.LIST_ELEMENT_CODE),
            eventComplexTypeEntities.get(0).getReference());
        assertEquals(definitionDataItem.getString(ColumnName.EVENT_ELEMENT_LABEL),
            eventComplexTypeEntities.get(0).getLabel());
        assertEquals(definitionDataItem.getString(ColumnName.EVENT_HINT_TEXT),
            eventComplexTypeEntities.get(0).getHint());
        assertEquals(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM),
            eventComplexTypeEntities.get(0).getLiveFrom());
        assertEquals(definitionDataItem.getLocalDate(ColumnName.LIVE_TO),
            eventComplexTypeEntities.get(0).getLiveTo());
        assertEquals(definitionDataItem.getInteger(ColumnName.FIELD_DISPLAY_ORDER),
            eventComplexTypeEntities.get(0).getOrder());
        assertEquals(DisplayContext.OPTIONAL, eventComplexTypeEntities.get(0).getDisplayContext());
        assertEquals(definitionDataItem.getBooleanOrDefault(ColumnName.PUBLISH, false),
                     eventComplexTypeEntities.get(0).getPublish());
        assertEquals(definitionDataItem.getString(ColumnName.PUBLISH_AS),
                     eventComplexTypeEntities.get(0).getPublishAs());
        assertFalse(eventComplexTypeEntities.get(0).getRetainHiddenValue());
    }

    private DefinitionDataItem definitionDataItem(String caseFieldId,
                                                  DisplayContextColumn displayContext,
                                                  String showCondition,
                                                  String label,
                                                  String hint,
                                                  String displayContextParameter,
                                                  Boolean publish,
                                                  String publishAs) {
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);

        when(definitionDataItem.getString(eq(ColumnName.CASE_FIELD_ID))).thenReturn(caseFieldId);
        when(definitionDataItem.getDisplayContext()).thenReturn(displayContext);
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn(showCondition);
        when(definitionDataItem.getString(ColumnName.CASE_EVENT_FIELD_LABEL)).thenReturn(label);
        when(definitionDataItem.getString(ColumnName.CASE_EVENT_FIELD_HINT)).thenReturn(hint);
        when(definitionDataItem.getInteger(ColumnName.FIELD_DISPLAY_ORDER)).thenReturn(1);
        when(definitionDataItem.getString(ColumnName.DISPLAY_CONTEXT_PARAMETER)).thenReturn(displayContextParameter);
        when(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM)).thenReturn(LIVE_FROM);
        when(definitionDataItem.getLocalDate(ColumnName.LIVE_TO)).thenReturn(LIVE_TO);
        when(definitionDataItem.getString(ColumnName.LIST_ELEMENT_CODE)).thenReturn(REFERENCE_ID);
        when(definitionDataItem.getBooleanOrDefault(ColumnName.PUBLISH, false)).thenReturn(publish);
        when(definitionDataItem.getString(ColumnName.PUBLISH_AS)).thenReturn(publishAs);
        when(definitionDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE)).thenReturn(false);
        return definitionDataItem;
    }
}
