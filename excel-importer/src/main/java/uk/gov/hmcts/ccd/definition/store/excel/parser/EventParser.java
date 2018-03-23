package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.SecurityClassificationColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class EventParser {
    private static final Logger logger = LoggerFactory.getLogger(EventParser.class);
    public static final String WILDCARD = "*";
    public static final String PRE_STATE_SEPARATOR = ";";
    public static final String WEBHOOK_RETRIES_SEPARATOR = ",";

    private final ParseContext parseContext;
    private final EventCaseFieldParser eventCaseFieldParser;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    public EventParser(ParseContext parseContext,
                       EventCaseFieldParser eventCaseFieldParser,
                       EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.parseContext = parseContext;
        this.eventCaseFieldParser = eventCaseFieldParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public Collection<EventEntity> parseAll(Map<String, DefinitionSheet> definitionSheets, CaseTypeEntity caseType) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing events for case type {}...", caseTypeId);

        final List<EventEntity> events = Lists.newArrayList();

        final Map<String, List<DefinitionDataItem>> eventItemsByCaseTypes = definitionSheets.get(SheetName.CASE_EVENT.getName())
                                                                                            .groupDataItemsByCaseType();

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
        final String postStateId = eventDefinition.getString(ColumnName.POST_CONDITION_STATE);
        if (!StringUtils.isBlank(postStateId) && !WILDCARD.equals(postStateId)) {
            event.setPostState(parseContext.getStateForCaseType(caseTypeId, postStateId));
        }

        // Webhooks
        event.setWebhookStart(parseWebhook(eventDefinition, ColumnName.CALLBACK_URL_ABOUT_TO_START_EVENT, ColumnName.RETRIES_TIMEOUT_ABOUT_TO_START_EVENT));
        event.setWebhookPreSubmit(parseWebhook(eventDefinition, ColumnName.CALLBACK_URL_ABOUT_TO_SUBMIT_EVENT, ColumnName.RETRIES_TIMEOUT_URL_ABOUT_TO_SUBMIT_EVENT));
        event.setWebhookPostSubmit(parseWebhook(eventDefinition, ColumnName.CALLBACK_URL_SUBMITTED_EVENT, ColumnName.RETRIES_TIMEOUT_URL_SUBMITTED_EVENT));

        return event;
    }

    private WebhookEntity parseWebhook(DefinitionDataItem eventDefinition, ColumnName urlColumn, ColumnName retriesColumn) {
        WebhookEntity webhook = null;

        final String url = eventDefinition.getString(urlColumn);
        if (!StringUtils.isBlank(url)) {
            webhook = new WebhookEntity();
            webhook.setUrl(url);

            final String retriesRaw = eventDefinition.getString(retriesColumn);
            if (!StringUtils.isBlank(retriesRaw)) {
                for (String retry : retriesRaw.split(WEBHOOK_RETRIES_SEPARATOR)) {
                    webhook.addTimeout(Integer.valueOf(retry));
                }
            }
        }

        return webhook;
    }

    private void parseEventCaseFields(CaseTypeEntity caseType, List<EventEntity> events, Map<String, DefinitionSheet> definitionSheets) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing event case fields for case type {}...", caseTypeId);

        final Map<String, List<DefinitionDataItem>> eventCaseFieldItemsByCaseTypes = definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName())
                                                                                                     .groupDataItemsByCaseType();

        if (!eventCaseFieldItemsByCaseTypes.containsKey(caseTypeId)) {
            logger.info("Parsing event case fields for case type {}: No event case fields found", caseTypeId);
            return;
        }

        final Map<String, List<DefinitionDataItem>> eventCaseFieldItemsByEvents = eventCaseFieldItemsByCaseTypes.get(caseTypeId)
                                                                                                                .stream()
                                                                                                                .collect(groupingBy(definitionDataItem -> definitionDataItem.getString(ColumnName.CASE_EVENT_ID)));
        for (EventEntity event : events) {
            logger.debug("Parsing event case fields for case type {} and event {}...", caseTypeId, event.getReference());

            final List<DefinitionDataItem> eventCaseFieldsItems = eventCaseFieldItemsByEvents.get(event.getReference());
            if (null == eventCaseFieldsItems || eventCaseFieldsItems.isEmpty()) {
                logger.info("Parsing event case fields for case type {} and event {}: No event case fields found", caseTypeId, event.getReference());
                continue;
            }

            for (DefinitionDataItem eventCaseFieldDefinition : eventCaseFieldsItems) {
                event.addEventCaseField(eventCaseFieldParser.parseEventCaseField(caseTypeId, eventCaseFieldDefinition));
            }

            logger.info("Parsing event case fields for case type {} and event {}: OK: {} case fields parsed", caseTypeId, event.getReference(), event.getEventCaseFields().size());
        }


        logger.debug("Parsing event case fields for case type {}: OK", caseTypeId);
    }

}
