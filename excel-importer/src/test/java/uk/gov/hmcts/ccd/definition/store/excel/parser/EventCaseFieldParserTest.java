package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DisplayContextColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventCaseFieldParserTest {

    @Mock
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @Mock
    private ShowConditionParser showConditionParser;

    @Mock
    private HiddenFieldsValidator hiddenFieldsValidator;

    @Mock
    private ParseContext parseContext;

    @InjectMocks
    private EventCaseFieldParser classUnderTest;

    private static final String PARSED_SHOW_CONDITION = "Parsed Show Condition";

    private static final CaseFieldEntity CASE_FIELD = new CaseFieldEntity();

    @Before
    public void setUp() throws InvalidShowConditionException {
        MockitoAnnotations.initMocks(this);
        when(parseContext.getCaseFieldForCaseType(any(), any())).thenReturn(CASE_FIELD);
        when(showConditionParser.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().showConditionExpression(PARSED_SHOW_CONDITION).build()
        );
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void defDataItemHasValidShowConditionParseEveCaseFldCalledParsedEventEntityWithShowCondtionSetToRsltOfShowConditionParsngAddToRegstryReturned() {

        String caseFieldId = "Case Field Id";
        String caseTypeId = "Case Type Id";
        String originalShowCondition = "Original Show Condition";
        String label = "label";
        String hint = "hint";
        DisplayContextColumn displayContext = new DisplayContextColumn("OPTIONAL", DisplayContext.OPTIONAL);

        DefinitionDataItem definitionDataItem = definitionDataItem(
            caseFieldId, displayContext, originalShowCondition, label, hint, false, false, null, null);
        when(hiddenFieldsValidator.parseHiddenFields(definitionDataItem)).thenReturn(Boolean.FALSE);
        EventCaseFieldEntity eventCaseFieldEntity = classUnderTest.parseEventCaseField(caseTypeId, definitionDataItem);

        assertEquals(CASE_FIELD, eventCaseFieldEntity.getCaseField());
        assertEquals(displayContext.getDisplayContext(), eventCaseFieldEntity.getDisplayContext());
        assertEquals(PARSED_SHOW_CONDITION, eventCaseFieldEntity.getShowCondition());
        assertFalse(eventCaseFieldEntity.getRetainHiddenValue());
        assertFalse(eventCaseFieldEntity.getPublish());
        assertNull(eventCaseFieldEntity.getPublishAs());
        assertEquals(label, eventCaseFieldEntity.getLabel());
        assertEquals(hint, eventCaseFieldEntity.getHintText());

        verify(entityToDefinitionDataItemRegistry)
            .addDefinitionDataItemForEntity(eq(eventCaseFieldEntity), eq(definitionDataItem));
        verify(parseContext).getCaseFieldForCaseType(eq(caseTypeId), eq(caseFieldId));
        verify(definitionDataItem).getString(ColumnName.CASE_EVENT_FIELD_LABEL);
        verify(definitionDataItem).getString(ColumnName.CASE_EVENT_FIELD_HINT);
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void defDataItemHasInValidShowConditionParseEventCaseFldCalledParsedEventEntityWithShowConditionSetToOrgShowConditionAddedToRegistryAndReturned()
        throws InvalidShowConditionException {

        String caseFieldId = "Case Field Id";
        String caseTypeId = "Case Type Id";
        String originalShowCondition = "Original Show Condition";
        String publishAs = "publishAsTest";
        DisplayContextColumn displayContext = new DisplayContextColumn("OPTIONAL", DisplayContext.OPTIONAL);

        DefinitionDataItem definitionDataItem = definitionDataItem(caseFieldId, displayContext, originalShowCondition,
            null, null, true, true, publishAs, null);

        when(showConditionParser.parseShowCondition(any())).thenThrow(
            new InvalidShowConditionException("")
        );
        when(hiddenFieldsValidator.parseHiddenFields(definitionDataItem)).thenReturn(Boolean.TRUE);
        EventCaseFieldEntity eventCaseFieldEntity = classUnderTest.parseEventCaseField(caseTypeId, definitionDataItem);

        assertEquals(CASE_FIELD, eventCaseFieldEntity.getCaseField());
        assertEquals(displayContext.getDisplayContext(), eventCaseFieldEntity.getDisplayContext());
        assertEquals(originalShowCondition, eventCaseFieldEntity.getShowCondition());
        assertTrue(eventCaseFieldEntity.getRetainHiddenValue());
        assertEquals(Boolean.TRUE, eventCaseFieldEntity.getPublish());
        assertEquals(publishAs, eventCaseFieldEntity.getPublishAs());

        verify(entityToDefinitionDataItemRegistry).addDefinitionDataItemForEntity(
            eq(eventCaseFieldEntity), eq(definitionDataItem));
        verify(parseContext).getCaseFieldForCaseType(eq(caseTypeId), eq(caseFieldId));
    }

    @Test
    public void shouldErrorWhenDisplayContextIsComplexAndPublishIsSpecified() {
        String expectedError = "Publish column must not be set for case field 'FieldId', "
            + "event 'EventId' in CaseEventToFields when DisplayContext is COMPLEX. "
            + "Please only use the Publish overrides in EventToComplexTypes.";

        String caseFieldId = "FieldId";
        String caseTypeId = "Case Type Id";
        String caseEventId = "EventId";
        DisplayContextColumn displayContext = new DisplayContextColumn("COMPLEX", DisplayContext.COMPLEX);

        DefinitionDataItem definitionDataItem = definitionDataItem(caseFieldId, displayContext, null,
            null, null, true, true, null, caseEventId);

        MapperException exception = assertThrows(MapperException.class,
            () -> classUnderTest.parseEventCaseField(caseTypeId, definitionDataItem));

        assertEquals(expectedError, exception.getMessage());
    }

    private DefinitionDataItem definitionDataItem(String caseFieldId,
                                                  DisplayContextColumn displayContext,
                                                  String showCondition,
                                                  String label,
                                                  String hint,
                                                  Boolean retainHiddenValue,
                                                  Boolean publish,
                                                  String publishAs,
                                                  String caseEventId) {
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);

        when(definitionDataItem.getString(eq(ColumnName.CASE_FIELD_ID))).thenReturn(caseFieldId);
        when(definitionDataItem.getDisplayContext()).thenReturn(displayContext);
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn(showCondition);
        when(definitionDataItem.getBoolean(eq(ColumnName.RETAIN_HIDDEN_VALUE))).thenReturn(retainHiddenValue);
        when(definitionDataItem.getString(ColumnName.CASE_EVENT_FIELD_LABEL)).thenReturn(label);
        when(definitionDataItem.getString(ColumnName.CASE_EVENT_FIELD_HINT)).thenReturn(hint);
        when(definitionDataItem.getBooleanOrDefault(ColumnName.PUBLISH, false)).thenReturn(publish);
        when(definitionDataItem.getString(ColumnName.CASE_EVENT_ID)).thenReturn(caseEventId);
        when(definitionDataItem.getString(ColumnName.PUBLISH_AS)).thenReturn(publishAs);

        return definitionDataItem;
    }
}
