package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

@Slf4j
public class SearchAliasFieldParser {

    private final ParseContext parseContext;

    public SearchAliasFieldParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public List<SearchAliasFieldEntity> parseAll(Map<String, DefinitionSheet> definitionSheets, CaseTypeEntity caseType) {
        Map<String, List<DefinitionDataItem>> searchAliasFieldsByCaseTypes = ofNullable(definitionSheets.get(SheetName.SEARCH_ALIAS.getName()))
            .map(DefinitionSheet::groupDataItemsByCaseType)
            .orElse(Collections.emptyMap());

        log.debug("Parsing search alias case fields for case type {}...", caseType.getReference());

        List<SearchAliasFieldEntity> searchAliasFields = ofNullable(searchAliasFieldsByCaseTypes.get(caseType.getReference()))
            .map(dataItems -> dataItems
                .stream()
                .map(dataItem -> parseSearchAliasField(dataItem, caseType))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        log.info("Parsed {} search alias fields for case type {}", searchAliasFields.size(), caseType.getReference());

        return searchAliasFields;
    }

    private SearchAliasFieldEntity parseSearchAliasField(DefinitionDataItem dataItem, CaseTypeEntity caseType) {
        SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
        searchAliasField.setCaseType(caseType);
        searchAliasField.setReference(dataItem.getString(ColumnName.SEARCH_ALIAS_ID));
        searchAliasField.setLiveFrom(dataItem.getLocalDate(ColumnName.LIVE_FROM));
        searchAliasField.setLiveTo(dataItem.getLocalDate(ColumnName.LIVE_TO));
        searchAliasField.setCaseFieldPath(dataItem.getString(ColumnName.CASE_FIELD_ID));

        return searchAliasField;
    }
}
