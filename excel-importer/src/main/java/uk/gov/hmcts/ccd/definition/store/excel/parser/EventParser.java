package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.SecurityClassificationColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.WebhookParser.parseWebhook;

public class EventParser {
    private static final Logger logger = LoggerFactory.getLogger(EventParser.class);
    private static final String WILDCARD = "*";
    private static final String PRE_STATE_SEPARATOR = ";";

    private final ParseContext parseContext;
    private final EventCaseFieldParser eventCaseFieldParser;
    private final EventCaseFieldComplexTypeParser eventCaseFieldComplexTypeParser;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    public EventParser(ParseContext parseContext,
                       EventCaseFieldParser eventCaseFieldParser,
                       EventCaseFieldComplexTypeParser eventCaseFieldComplexTypeParser,
                       EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.parseContext = parseContext;
        this.eventCaseFieldParser = eventCaseFieldParser;
        this.eventCaseFieldComplexTypeParser = eventCaseFieldComplexTypeParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public Collection<EventEntity> parseAll(Map<String, DefinitionSheet> definitionSheets, CaseTypeEntity caseType) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing events for case type {}...", caseTypeId);

        final List<EventEntity> events = Lists.newArrayList();

        final Map<String, List<DefinitionDataItem>> eventItemsByCaseTypes = definitionSheets.get(
            SheetName.CASE_EVENT.getName()).groupDataItemsByCaseType();

        if (!eventItemsByCaseTypes.containsKey(caseTypeId)) {
            throw new SpreadsheetParsingException("At least one event must be defined for case type: " + caseTypeId);
        }

        final List<DefinitionDataItem> eventItems = eventItemsByCaseTypes.get(caseTypeId);

        logger.debug("Parsing events for case type {}: {} events detected", caseTypeId, eventItems.size());

        for (DefinitionDataItem eventDefinition : eventItems) {
            final String eventId = eventDefinition.getId();
            logger.debug("Parsing events for case type {}: Parsing event {}...", caseTypeId, eventId);

            final EventEntity event = parseEvent(caseTypeId, eventId, eventDefinition);
            this.entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(event, eventDefinition);
            events.add(event);
            parseContext.registerEvent(caseTypeId, event);

            logger.info("Parsing events for case type {}: Parsing event {}: OK", caseTypeId, eventId);
        }

        parseEventCaseFields(caseType, events, definitionSheets);

        parseCaseEventComplexTypes(definitionSheets, events);

        logger.info("Parsing events for case type {}: OK: {} case fields parsed", caseTypeId, events.size());

        return events;
    }

    private EventEntity parseEvent(String caseTypeId, String eventId, DefinitionDataItem eventDefinition) {
        final EventEntity event = new EventEntity();

        event.setReference(eventId);
        event.setLiveFrom(eventDefinition.getLocalDate(ColumnName.LIVE_FROM));
        event.setLiveTo(eventDefinition.getLocalDate(ColumnName.LIVE_TO));
        event.setName(eventDefinition.getString(ColumnName.NAME));
        event.setDescription(eventDefinition.getString(ColumnName.DESCRIPTION));
        event.setOrder(eventDefinition.getInteger(ColumnName.DISPLAY_ORDER));
        event.setShowSummary(eventDefinition.getBoolean(ColumnName.SHOW_SUMMARY));
        event.setShowEventNotes(eventDefinition.getBoolean(ColumnName.SHOW_EVENT_NOTES));
        event.setCanSaveDraft(eventDefinition.getBoolean(ColumnName.CAN_SAVE_DRAFT));
        event.setEndButtonLabel(eventDefinition.getString(ColumnName.END_BUTTON_LABEL));

        SecurityClassificationColumn securityClassificationColumn = eventDefinition.getSecurityClassification();
        event.setSecurityClassification(securityClassificationColumn.getSecurityClassification());

        // Pre-states
        final String preStates = eventDefinition.getString(ColumnName.PRE_CONDITION_STATE);
        if (StringUtils.isBlank(preStates)) {
            // Create only
            event.setCanCreate(Boolean.TRUE);
        } else if (WILDCARD.equals(preStates)) {
            // All events, no creation
            event.setCanCreate(Boolean.FALSE);
        } else {
            // Some events, no creation
            event.setCanCreate(Boolean.FALSE);
            for (String preStateId : preStates.split(PRE_STATE_SEPARATOR)) {
                event.addPreState(parseContext.getStateForCaseType(caseTypeId, preStateId));
            }
        }

        // Post-state
        EventPostStateParser postStateParser = new EventPostStateParser(parseContext, caseTypeId);
        final String postStateId = eventDefinition.getString(ColumnName.POST_CONDITION_STATE);
        event.addEventPostStates(postStateParser.parse(postStateId));

        // Webhooks
        event.setWebhookStart(parseWebhook(eventDefinition,
            ColumnName.CALLBACK_URL_ABOUT_TO_START_EVENT,
            ColumnName.RETRIES_TIMEOUT_ABOUT_TO_START_EVENT));
        event.setWebhookPreSubmit(parseWebhook(eventDefinition,
            ColumnName.CALLBACK_URL_ABOUT_TO_SUBMIT_EVENT,
            ColumnName.RETRIES_TIMEOUT_URL_ABOUT_TO_SUBMIT_EVENT));
        event.setWebhookPostSubmit(parseWebhook(eventDefinition,
            ColumnName.CALLBACK_URL_SUBMITTED_EVENT,
            ColumnName.RETRIES_TIMEOUT_URL_SUBMITTED_EVENT));

        return event;
    }

    private void parseEventCaseFields(CaseTypeEntity caseType,
                                      List<EventEntity> events,
                                      Map<String, DefinitionSheet> definitionSheets) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing event case fields for case type {}...", caseTypeId);

        final Map<String, List<DefinitionDataItem>> eventCaseFieldItemsByCaseTypes = definitionSheets.get(
            SheetName.CASE_EVENT_TO_FIELDS.getName())
            .groupDataItemsByCaseType();

        if (!eventCaseFieldItemsByCaseTypes.containsKey(caseTypeId)) {
            logger.info("Parsing event case fields for case type {}: No event case fields found", caseTypeId);
            return;
        }

        final Map<String, List<DefinitionDataItem>> eventCaseFieldItemsByEvents
            = eventCaseFieldItemsByCaseTypes.get(caseTypeId).stream()
            .collect(groupingBy(definitionDataItem -> definitionDataItem.getString(ColumnName.CASE_EVENT_ID)));
        for (EventEntity event : events) {
            logger.debug("Parsing event case fields for case type {} and event {}...",
                caseTypeId, event.getReference());

            final List<DefinitionDataItem> eventCaseFieldsItems = eventCaseFieldItemsByEvents.get(event.getReference());
            if (null == eventCaseFieldsItems || eventCaseFieldsItems.isEmpty()) {
                logger.info("Parsing event case fields for case type {} and event {}: No event case fields found",
                    caseTypeId, event.getReference());
                continue;
            }

            for (DefinitionDataItem eventCaseFieldDefinition : eventCaseFieldsItems) {
                event.addEventCaseField(eventCaseFieldParser.parseEventCaseField(caseTypeId, eventCaseFieldDefinition));
            }

            logger.info("Parsing event case fields for case type {} and event {}: OK: {} case fields parsed",
                caseTypeId, event.getReference(), event.getEventCaseFields().size());
        }


        logger.debug("Parsing event case fields for case type {}: OK", caseTypeId);
    }

    private void parseCaseEventComplexTypes(Map<String, DefinitionSheet> definitionSheets,
                                            List<EventEntity> events) {
        if (definitionSheets.containsKey(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName())) {
            Map<Pair<String, String>, List<DefinitionDataItem>> caseEventToComplexTypeByEventIdAndCaseFieldId =
                definitionSheets
                    .get(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName())
                    .getDataItems()
                    .stream()
                    .collect(groupingBy(p ->
                        Pair.of(p.getString(ColumnName.CASE_EVENT_ID), p.getString(ColumnName.CASE_FIELD_ID))));

            for (EventEntity event : events) {
                for (EventCaseFieldEntity eventCaseFieldEntity : event.getEventCaseFields()) {
                    List<DefinitionDataItem> definitionDataItems = caseEventToComplexTypeByEventIdAndCaseFieldId
                        .get(Pair.of(
                            eventCaseFieldEntity.getEvent().getReference(),
                            eventCaseFieldEntity.getCaseField().getReference()));

                    if (eventCaseFieldEntity.getDisplayContext() == DisplayContext.COMPLEX) {
                        eventCaseFieldEntity.addComplexFields(eventCaseFieldComplexTypeParser
                            .parseEventCaseFieldComplexType(definitionDataItems));
                    }
                }
            }
        }
    }
}
