package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.common.TestLoggerUtils;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.common.TestLoggerUtils.assertLogged;

@ExtendWith(MockitoExtension.class)
class EventParserTest extends ParserTestBase {

    private static final String EVENT_ID = "event id";
    private static final String FIELD_ID = "field id";
    private EventParser eventParser;
    private DefinitionSheet caseEventToFieldsSheet;
    private DefinitionSheet caseEventToComplexTypesSheet;

    @Mock
    private EventCaseFieldParser eventCaseFieldParser;

    @Mock
    private EventCaseFieldComplexTypeParser eventCaseFieldComplexTypeParser;

    @Mock
    private HiddenFieldsValidator hiddenFieldsValidator;

    @Mock
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    private ListAppender<ILoggingEvent> listAppender;

    private boolean assertEntityAddedToRegistry = true;

    private final DefinitionDataItem item = buildSimpleDefinitionDataItem();

    private EventEntity entity;

    private ShowConditionParser showConditionParser;

    @BeforeEach
    void setup() {
        init();
        parseContext = mock(ParseContext.class);
        showConditionParser = new ShowConditionParser();
        eventParser = new EventParser(
            parseContext,
            eventCaseFieldParser,
            eventCaseFieldComplexTypeParser,
            entityToDefinitionDataItemRegistry,
            showConditionParser,
            true);

        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);

        definitionSheets.put(SheetName.CASE_EVENT.getName(), definitionSheet);
        caseEventToFieldsSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.CASE_EVENT_TO_FIELDS.getName(), caseEventToFieldsSheet);
        caseEventToComplexTypesSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName(), caseEventToComplexTypesSheet);

        listAppender = TestLoggerUtils.setupLogger();
    }

    @AfterEach
    void teardown() {
        TestLoggerUtils.teardownLogger();

        if (assertEntityAddedToRegistry) {
            verify(entityToDefinitionDataItemRegistry).addDefinitionDataItemForEntity(entity, item);
        }
    }

    @Test
    void shouldFail_whenDefinitionSheetNotDefined() {
        assertEntityAddedToRegistry = false;
        SpreadsheetParsingException ex = assertThrows(SpreadsheetParsingException.class, () ->
            eventParser.parseAll(definitionSheets, caseType));
        assertThat(ex.getMessage(), is("At least one event must be defined for case type: Some Case Type"));
    }

    @Test
    void shouldEventPreState_whenParseSuccess() {
        item.addAttribute(ColumnName.PRE_CONDITION_STATE.toString(), "*");
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
    }

    @Test
    void shouldEventPreStateContent_whenParseSuccess() {
        final StateEntity state = mock(StateEntity.class);
        final String preState = "CaseEnteredIntoLegacy";

        given(parseContext.getStateForCaseType(CASE_TYPE_UNDER_TEST, preState)).willReturn(state);

        item.addAttribute(ColumnName.PRE_CONDITION_STATE.toString(), preState);
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
        assertThat(entity.getPreStates().get(0), is(state));
    }

    @Test
    void shouldParseEventWildcardPostStateContent_whenParseSuccess() {
        final StateEntity state = mock(StateEntity.class);
        final String preState = "CaseEnteredIntoLegacy";

        given(parseContext.getStateForCaseType(CASE_TYPE_UNDER_TEST, preState)).willReturn(state);

        item.addAttribute(ColumnName.PRE_CONDITION_STATE.toString(), preState);
        item.addAttribute(ColumnName.POST_CONDITION_STATE.toString(), "*");
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
        assertThat(entity.getPostStates().size(), is(1));
        assertThat(entity.getPreStates().get(0), is(state));
    }

    @Test
    void shouldParseEventPostStateContent_whenParseSuccess() {
        final StateEntity state = mock(StateEntity.class);
        when(state.getReference()).thenReturn("Post state");
        given(parseContext.getStateForCaseType(any(), any())).willReturn(state);

        item.addAttribute(ColumnName.PRE_CONDITION_STATE.toString(), "CaseCreated");
        item.addAttribute(ColumnName.PRE_CONDITION_STATE.toString(), "CaseEnteredIntoLegacy");
        item.addAttribute(ColumnName.POST_CONDITION_STATE.toString(), "Post state");
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
        assertThat(entity.getPostStates().get(0).getPostStateReference(), is(state.getReference()));
    }

    @Test
    void shouldParseWebHook() {
        item.addAttribute(ColumnName.CALLBACK_URL_ABOUT_TO_START_EVENT.toString(), "webhook");
        item.addAttribute(ColumnName.RETRIES_TIMEOUT_ABOUT_TO_START_EVENT.toString(), "45,57,98");
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
        assertThat(entity.getWebhookStart().getUrl(), is("webhook"));
        assertThat(entity.getWebhookStart().getTimeouts(), contains(45, 57, 98));
    }

    @Test
    void shouldParseEvent() {
        definitionSheet.addDataItem(item);
        DefinitionDataItem caseEventToFieldsDataItem = buildCaseEventToFieldsDataItem(EVENT_ID);
        caseEventToFieldsSheet.addDataItem(caseEventToFieldsDataItem);

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseFieldEntity(FIELD_ID));
        when(eventCaseFieldParser.parseEventCaseField(any(), any())).thenReturn(eventCaseFieldEntity);

        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);

        verify(eventCaseFieldParser).parseEventCaseField(CASE_TYPE_UNDER_TEST, caseEventToFieldsDataItem);

        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);

        assertEvent(entity);
        assertThat(entity.getPostStates().size(), is(0));
        assertThat(entity.getPreStates(), empty());
    }

    @Test
    void shouldParseEventWithoutRestrictionDataItem() {
        definitionSheet.addDataItem(item);
        caseEventToFieldsSheet.addDataItem(buildCaseEventToFieldsDataItem("EVENT_ID"));
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
        assertThat(entity.getPostStates().size(), is(0));
        assertThat(entity.getPreStates(), empty());

        assertLogged(listAppender,
            "Parsing event case fields for case type Some Case Type and event event id: No event case fields"
                + " found");
    }

    @Test
    void shouldParseEventWithCaseFieldComplexType() {
        definitionSheet.addDataItem(item);
        caseEventToFieldsSheet.addDataItem(buildCaseEventToFieldsDataItem(EVENT_ID));
        DefinitionDataItem caseEventToComplexTypesDataItem = buildCaseEventToComplexTypesDataItem(EVENT_ID, FIELD_ID);
        caseEventToComplexTypesSheet.addDataItem(caseEventToComplexTypesDataItem);

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseFieldEntity(FIELD_ID));
        eventCaseFieldEntity.setDisplayContext(DisplayContext.COMPLEX);
        when(eventCaseFieldParser.parseEventCaseField(any(), any())).thenReturn(eventCaseFieldEntity);

        EventComplexTypeEntity eventComplexTypeEntityMock = mock(EventComplexTypeEntity.class);
        when(eventCaseFieldComplexTypeParser.parseEventCaseFieldComplexType(any(), any()))
            .thenReturn(Arrays.asList(eventComplexTypeEntityMock));

        final Collection<EventEntity> events = eventParser.parseAll(definitionSheets, caseType);

        verify(eventCaseFieldComplexTypeParser)
            .parseEventCaseFieldComplexType(singletonList(caseEventToComplexTypesDataItem), definitionSheets);

        assertThat(events.size(), is(1));
        entity = new ArrayList<>(events).get(0);
    }

    @Test
    void shouldParseEventWithEventEnablingCondition() {
        final String validEventEnablingCondition = "FieldA!=\"\" AND FieldB=\"I'm innocent\"";
        item.addAttribute(ColumnName.EVENT_ENABLING_CONDITION.toString(), validEventEnablingCondition);
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
    }

    @Test
    void shouldParseEventWithChainedEventEnablingCondition() {
        final String validEventEnablingCondition = "(FieldA!=\"\" AND FieldB=\"I'm innocent\") "
            + "OR (FieldC=\"I'm guilty\")";
        item.addAttribute(ColumnName.EVENT_ENABLING_CONDITION.toString(), validEventEnablingCondition);
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
    }

    @Test
    void shouldFailParseEventWithEmptyEventEnablingCondition() {
        final String validEventEnablingCondition = " ";
        item.addAttribute(ColumnName.EVENT_ENABLING_CONDITION.toString(), validEventEnablingCondition);
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertEvent(entity);
    }

    @Test
    void shouldFailParseEventWithInvalidEventEnablingCondition() {
        assertEntityAddedToRegistry = false;
        final String validEventEnablingCondition = "aaa. x.bbb=\"some-value\"";
        item.addAttribute(ColumnName.EVENT_ENABLING_CONDITION.toString(), validEventEnablingCondition);
        definitionSheet.addDataItem(item);
        assertThrows(SpreadsheetParsingException.class, () -> 
            eventParser.parseAll(definitionSheets, caseType));
    }

    @Test
    void shouldAssignDefaultPublishIfColumnNotExists() {
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertThat(entity.getPublish(), is(true));
    }

    @Test
    void shouldAssignDefaultPublishIfColumnHasNullValue() {
        item.addAttribute(ColumnName.PUBLISH.toString(), null);
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertThat(entity.getPublish(), is(true));
    }

    @Test
    void shouldAssignPublishValueFromColumn() {
        item.addAttribute(ColumnName.PUBLISH.toString(), "N");
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertThat(entity.getPublish(), is(false));
    }

    @Test
    void shouldAssignTtlIncrementValueFromColumn() {
        item.addAttribute(ColumnName.TTL_INCREMENT.toString(), 1);
        definitionSheet.addDataItem(item);
        final Collection<EventEntity> eventEntities = eventParser.parseAll(definitionSheets, caseType);
        assertThat(eventEntities.size(), is(1));
        entity = new ArrayList<>(eventEntities).get(0);
        assertThat(entity.getTtlIncrement(), is(1));
    }

    private void assertEvent(final EventEntity entity) {
        assertThat(entity.getReference(), is(EVENT_ID));
        assertThat(entity.getName(), is("event name"));
        assertThat(entity.getDescription(), is("event Description"));
        assertThat(entity.getEndButtonLabel(), is("End Button Label"));
        assertThat(entity.getCanSaveDraft(), is(true));
    }

    private DefinitionDataItem buildSimpleDefinitionDataItem() {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.ID.toString(), EVENT_ID);
        item.addAttribute(ColumnName.NAME.toString(), "event name");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "event Description");
        item.addAttribute(ColumnName.END_BUTTON_LABEL.toString(), "End Button Label");
        item.addAttribute(ColumnName.CAN_SAVE_DRAFT.toString(), "Y");
        return item;
    }

    private DefinitionDataItem buildCaseEventToFieldsDataItem(final String eventId) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), eventId);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), "event fields");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        return item;
    }

    private DefinitionDataItem buildCaseEventToComplexTypesDataItem(final String eventId, final String fieldId) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName());
        item.addAttribute(ColumnName.CASE_EVENT_ID.toString(), eventId);
        item.addAttribute(ColumnName.CASE_FIELD_ID.toString(), fieldId);
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), "event fields");
        item.addAttribute(ColumnName.EVENT_ELEMENT_LABEL.toString(), "event fields");
        item.addAttribute(ColumnName.EVENT_HINT_TEXT.toString(), "event fields");
        item.addAttribute(ColumnName.FIELD_DISPLAY_ORDER.toString(), "event fields");
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "READONLY");
        item.addAttribute(ColumnName.FIELD_SHOW_CONDITION.toString(), "READONLY");
        return item;
    }

    private CaseFieldEntity caseFieldEntity(final String fieldId) {
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference(fieldId);
        return caseField;
    }
}
