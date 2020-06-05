package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.*;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.*;

public class SearchCasesResultLayoutParser extends GenericLayoutParser {
    private static final Logger logger = LoggerFactory.getLogger(SearchCasesResultLayoutParser.class);

    public SearchCasesResultLayoutParser(final ParseContext parseContext,
                                         final EntityToDefinitionDataItemRegistry registry,
                                         final ShowConditionParser showConditionParser) {
        super(parseContext, registry, showConditionParser);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets) {
        DefinitionSheet definitionSheet = definitionSheets.get(SheetName.SEARCH_CASES_RESULT_FIELDS.getName());
        if (definitionSheet == null) {
            throw new MapperException(
                String.format("A definition must contain a %s sheet", SheetName.SEARCH_CASES_RESULT_FIELDS.getName())
            );
        }
        return definitionSheet;
    }

    @Override
    protected String getLayoutName() {
        return "searchCase";
    }

    @Override
    protected GenericLayoutEntity createLayoutCaseFieldEntity() {
        return new SearchCasesResultFieldEntity();
    }

    @Override
    protected void populateSortOrder(GenericLayoutEntity layoutEntity, String sortOrderString) {
        SearchCasesResultFieldEntity resultCaseFieldEntity = ((SearchCasesResultFieldEntity)layoutEntity);
        resultCaseFieldEntity.setSortOrder(getSortOrder(sortOrderString));
    }

    @Override
    protected void populateShowCondition(GenericLayoutEntity layoutEntity, String showCondition) {
        throw new MapperException(String.format("showCondition is not supported in worksheet '%s' for "
            + "caseType '%s'", SheetName.SEARCH_CASES_RESULT_FIELDS.getName(), layoutEntity.getCaseType().getReference()));
    }

    @Override
    protected void populateUseCase(GenericLayoutEntity layoutEntity, String useCase) {
        SearchCasesResultFieldEntity resultCaseFieldEntity = ((SearchCasesResultFieldEntity)layoutEntity);
        resultCaseFieldEntity.setUseCase(useCase.toUpperCase());
    }
}
