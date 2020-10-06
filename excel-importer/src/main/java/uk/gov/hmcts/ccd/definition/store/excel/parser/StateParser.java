package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StateParser {
    private static final Logger logger = LoggerFactory.getLogger(StateParser.class);

    private final ParseContext parseContext;

    public StateParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public Collection<StateEntity> parseAll(Map<String, DefinitionSheet> definitionSheets, CaseTypeEntity caseType) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing states for case type {}...", caseTypeId);

        final List<StateEntity> states = Lists.newArrayList();

        final Map<String, List<DefinitionDataItem>> stateItemsByCaseTypes = definitionSheets
            .get(SheetName.STATE.getName())
            .groupDataItemsByCaseType();

        if (!stateItemsByCaseTypes.containsKey(caseTypeId)) {
            throw new SpreadsheetParsingException("At least one state must be defined for case type: " + caseTypeId);
        }

        final List<DefinitionDataItem> stateItems = stateItemsByCaseTypes.get(caseTypeId);

        logger.debug("Parsing states for case type {}: {} states detected", caseTypeId, stateItems.size());

        for (DefinitionDataItem stateDefinition : stateItems) {
            final String stateId = stateDefinition.getId();
            logger.debug("Parsing states for case type {}: Parsing state {}...", caseTypeId, stateId);

            final StateEntity state = parseState(stateId, stateDefinition);
            parseContext.registerStateForCaseType(caseTypeId, state);
            states.add(state);

            logger.info("Parsing states for case type {}: Parsing state {}: OK", caseTypeId, stateId);
        }

        logger.info("Parsing states for case type {}: OK: {} case states parsed", caseTypeId, states.size());

        return states;
    }

    private StateEntity parseState(String stateId, DefinitionDataItem stateDefinition) {
        final StateEntity state = new StateEntity();

        state.setReference(stateId);
        state.setName(stateDefinition.getString(ColumnName.NAME));
        state.setDescription(stateDefinition.getString(ColumnName.DESCRIPTION));
        state.setOrder(stateDefinition.getInteger(ColumnName.DISPLAY_ORDER));
        state.setLiveFrom(stateDefinition.getLocalDate(ColumnName.LIVE_FROM));
        state.setLiveTo(stateDefinition.getLocalDate(ColumnName.LIVE_TO));
        state.setTitleDisplay(stateDefinition.getString(ColumnName.TITLE_DISPLAY));

        return state;
    }
}
