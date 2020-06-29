package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CRUD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.STATE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.USER_ROLE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_STATE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.STATE;

class AuthorisationCaseStateParser implements AuthorisationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationCaseStateParser.class);

    private final ParseContext parseContext;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    AuthorisationCaseStateParser(ParseContext parseContext,
                                        EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public Collection<StateACLEntity> parseAll(Map<String, DefinitionSheet> definitionSheets,
                                               CaseTypeEntity caseTypeEntity,
                                               StateEntity stateEntity) {
        Collection<StateACLEntity> parseResults = Lists.newArrayList();

        final String caseTypeReference = caseTypeEntity.getReference();
        final String stateReference = stateEntity.getReference();
        LOG.debug("Parsing AuthorisationCaseState for case type {}...", caseTypeReference);

        DefinitionSheet definitionSheet = getDefinitionSheet(definitionSheets);

        final Map<String, List<DefinitionDataItem>> dataItemMap = definitionSheet.groupDataItemsByCaseType();
        validateCaseTypes(definitionSheets, dataItemMap);
        validateStates(definitionSheets, definitionSheet, caseTypeReference);
        final List<DefinitionDataItem> dataItems = dataItemMap.get(caseTypeReference);

        if (null == dataItems) {
            LOG.warn("No row were defined for case state '{}' in AuthorisationCaseState tab", stateReference);
        } else {
            LOG.debug("Parsing user roles for case state {}: {} AuthorisationCaseSate detected",
                stateReference, dataItems.size());
            final Map<String, List<DefinitionDataItem>> collect = dataItems.stream().collect(groupingBy(d -> d.getString(STATE_ID)));
            if (null == collect.get(stateReference)) {
                LOG.warn("No row is defined for case type '{}', state '{}'", caseTypeReference, stateReference);
                // and let validation handles this Exception
            } else {
                for (DefinitionDataItem definition : collect.get(stateReference)) {
                    StateACLEntity entity = new StateACLEntity();

                    parseUserRole(entity, definition, parseContext);
                    parseCrud(entity, definition);
                    parseResults.add(entity);
                    entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(entity, definition);

                    LOG.info("Parsing user role for case type '{}', state '{}', user role '{}', crud '{}': OK",
                        caseTypeReference,
                        stateReference,
                        definition.getString(USER_ROLE), definition.getString(CRUD));
                }
            }
        }
        return parseResults;
    }

    private void validateStates(Map<String, DefinitionSheet> definitionSheets, DefinitionSheet definitionSheet, String caseTypeReference) {
        final Map<String, List<DefinitionDataItem>> statesWithAuthorisationInfoThisCaseType = definitionSheet.getDataItems()
            .stream()
            .filter(definitionDataItem -> definitionDataItem.getString(ColumnName.CASE_TYPE_ID).equalsIgnoreCase(caseTypeReference))
            .collect(groupingBy(dataItem -> dataItem.getString(ColumnName.STATE_ID)));

        final List<String> stateItemsForThisCaseType = definitionSheets.get(STATE.getName())
            .groupDataItemsByCaseType()
            .get(caseTypeReference)
            .stream()
            .map(definitionDataItem -> definitionDataItem.getString(ColumnName.ID)).collect(Collectors.toList());

        final Optional<String> unknownStateType = statesWithAuthorisationInfoThisCaseType.keySet()
            .stream()
            .filter(typeName -> !stateItemsForThisCaseType.contains(typeName))
            .findFirst();
        if (unknownStateType.isPresent()) {
            throw new MapperException(String.format("Unknown State '%s' for CaseType '%s' in worksheet '%s'",
                unknownStateType.get(),
                caseTypeReference,
                getSheetName()));
        }
    }

    @Override
    public String getSheetName() {
        return AUTHORISATION_CASE_STATE.getName();
    }
}
