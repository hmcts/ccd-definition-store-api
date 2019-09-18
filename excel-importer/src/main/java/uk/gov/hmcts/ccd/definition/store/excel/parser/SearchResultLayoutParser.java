package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;

import java.util.Map;

public class SearchResultLayoutParser extends GenericLayoutParser {
    private static final Logger logger = LoggerFactory.getLogger(SearchResultLayoutParser.class);

    public SearchResultLayoutParser(final ParseContext parseContext,
                                    final EntityToDefinitionDataItemRegistry registry) {
        super(parseContext, registry);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets) {
        DefinitionSheet definitionSheet = definitionSheets.get(SheetName.SEARCH_RESULT_FIELD.getName());
        if (definitionSheet == null) {
            throw new MapperException(
                String.format("A definition must contain a %s sheet", SheetName.SEARCH_RESULT_FIELD.getName())
            );
        }
        return definitionSheet;
    }

    @Override
    protected String getLayoutName() {
        return "Search Results";
    }

    @Override
    protected GenericLayoutEntity createLayoutCaseFieldEntity() {
        return new SearchResultCaseFieldEntity();
    }

    @Override
    protected void populateSortOrder(GenericLayoutEntity layoutEntity, String sortOrderString) {
        SearchResultCaseFieldEntity resultCaseFieldEntity = ((SearchResultCaseFieldEntity)layoutEntity);
        resultCaseFieldEntity.setSortOrder(getSortOrder(sortOrderString));
    }

}
