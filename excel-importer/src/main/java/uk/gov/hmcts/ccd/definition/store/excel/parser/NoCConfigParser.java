package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

import static java.util.Optional.ofNullable;

@Slf4j
public class NoCConfigParser {

    private ParseContext parseContext;


    public NoCConfigParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public Map<String, List<NoCConfigEntity>> parse(Map<String, DefinitionSheet> definitionSheets) {
        Map<String, List<DefinitionDataItem>> nocConfigsCaseType = ofNullable(definitionSheets.get(SheetName.NOC_CONFIG.getName()))
            .map(DefinitionSheet::groupDataItemsByCaseType)
            .orElse(Collections.emptyMap());

        Map<String, List<NoCConfigEntity>> caseTypeNoCConfigEntities = new HashMap<>();
        parseContext.getCaseTypes()
            .stream()
            .forEach(caseType -> {
                log.debug("Parsing noc config for case type {}...", caseType.getReference());

                Optional<List<DefinitionDataItem>> nocConfigDataItems = ofNullable(nocConfigsCaseType.get(caseType.getReference()));
                caseTypeNoCConfigEntities.put(caseType.getReference(), nocConfigDataItems
                    .map(dataItems -> dataItems
                        .stream()
                        .map(dataItem -> parseNoCConfigItems(dataItem, caseType))
                        .collect(Collectors.toList()))
                    .orElse(Collections.emptyList()));
            });

        return caseTypeNoCConfigEntities;
    }

    private NoCConfigEntity parseNoCConfigItems(DefinitionDataItem dataItem, CaseTypeEntity caseType) {
        NoCConfigEntity noCConfigEntity = new NoCConfigEntity();
        noCConfigEntity.setReasonsRequired(dataItem.getBoolean(ColumnName.REASON_REQUIRED));
        noCConfigEntity.setNocActionInterpretationRequired(dataItem.getBoolean(ColumnName.NOC_ACTION_INTERPRETATION_REQUIRED));
        noCConfigEntity.setCaseType(caseType);
        return noCConfigEntity;
    }
}
