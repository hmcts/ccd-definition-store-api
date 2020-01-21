package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;

class AuthorisationCaseEventParser implements AuthorisationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationCaseEventParser.class);

    private final ParseContext parseContext;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    AuthorisationCaseEventParser(final ParseContext parseContext, final EntityToDefinitionDataItemRegistry registry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = registry;
    }

    Collection<EventACLEntity> parseAll(final Map<String, DefinitionSheet> definitionSheets,
                                        final CaseTypeEntity caseType,
                                        final EventEntity event) {
        final List<EventACLEntity> parseResults = Lists.newArrayList();

        final String caseTypeReference = caseType.getReference();
        final String eventReference = event.getReference();
        LOG.debug("Parsing AuthorisationCaseEvent for case type {}, event {}...", caseTypeReference, eventReference);

        DefinitionSheet definitionSheet = getDefinitionSheet(definitionSheets);

        final Map<String, List<DefinitionDataItem>> dataItemMap = definitionSheet.groupDataItemsByCaseType();
        validateCaseTypes(definitionSheets, dataItemMap);
        validateCaseEvents(definitionSheets, definitionSheet, caseTypeReference);

        final List<DefinitionDataItem> dataItems = dataItemMap.get(caseTypeReference);

        if (null == dataItems) {
            LOG.warn("No data is found for case type '{} in AuthorisationCaseEvents tab", caseTypeReference);
        } else {
            LOG.debug("Parsing user roles for case type {}: {} AuthorisationCaseEvents detected", caseTypeReference, dataItems.size());

            final Map<String, List<DefinitionDataItem>> collect = dataItems.stream().collect(groupingBy(d -> d.getString(ColumnName.CASE_EVENT_ID)));

            if (null == collect.get(eventReference)) {
                LOG.warn("No row is defined for case type '{}', event '{}'", caseTypeReference, eventReference);
                // and let validation handles this Exception
            } else {
                for (DefinitionDataItem definition : collect.get(eventReference)) {
                    EventACLEntity entity = new EventACLEntity();

                    parseUserRole(entity, definition, parseContext);
                    parseCrud(entity, definition);
                    parseResults.add(entity);
                    entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(entity, definition);

                    LOG.info("Parsing user role for case type '{}', event '{}', user role '{}', crud '{}': OK", caseTypeReference, eventReference, definition.getString(ColumnName.USER_ROLE), definition.getString(ColumnName.CRUD));
                }
            }
        }

        return parseResults;
    }

    private void validateCaseEvents(Map<String, DefinitionSheet> definitionSheets, DefinitionSheet definitionSheet, String caseTypeReference) {
        final Map<String, List<DefinitionDataItem>> caseEventsWithAuthorisationInfoThisCaseType = definitionSheet.getDataItems()
            .stream()
            .filter(definitionDataItem -> definitionDataItem.getString(ColumnName.CASE_TYPE_ID).equalsIgnoreCase(caseTypeReference))
            .collect(groupingBy(dataItem -> dataItem.getString(ColumnName.CASE_EVENT_ID)));

        final List<String> caseEventItemsForThisCaseType = definitionSheets.get(CASE_EVENT.getName())
            .groupDataItemsByCaseType()
            .get(caseTypeReference)
            .stream()
            .map(definitionDataItem -> definitionDataItem.getString(ColumnName.ID)).collect(Collectors.toList());

        final Optional<String> unknownCaseEventId = caseEventsWithAuthorisationInfoThisCaseType.keySet()
            .stream()
            .filter(typeName -> !caseEventItemsForThisCaseType.contains(typeName))
            .findFirst();
        if (unknownCaseEventId.isPresent()) {
            throw new MapperException(String.format("Unknown CaseEvent '%s' for CaseType '%s' in worksheet '%s'", unknownCaseEventId.get(), caseTypeReference, getSheetName()));
        }
    }

    @Override
    public String getSheetName() {
        return AUTHORISATION_CASE_EVENT.getName();
    }
}
