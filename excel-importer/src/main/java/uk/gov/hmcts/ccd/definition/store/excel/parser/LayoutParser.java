package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Map;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

public class LayoutParser {

    private final WorkbasketInputLayoutParser workbasketInputLayoutParser;
    private final WorkbasketLayoutParser workbasketLayoutParser;
    private final SearchCasesResultLayoutParser searchCasesResultLayoutParser;
    private final SearchInputLayoutParser searchInputLayoutParser;
    private final SearchResultLayoutParser searchResultLayoutParser;
    private final CaseTypeTabParser caseTypeTabParser;
    private final WizardPageParser wizardPageParser;

    public LayoutParser(WorkbasketInputLayoutParser workbasketInputLayoutParser,
                        WorkbasketLayoutParser workbasketLayoutParser,
                        SearchInputLayoutParser searchInputLayoutParser,
                        SearchResultLayoutParser searchResultLayoutParser,
                        CaseTypeTabParser caseTypeTabParser,
                        WizardPageParser wizardPageParser,
                        SearchCasesResultLayoutParser searchCasesResultLayoutParser) {
        this.workbasketInputLayoutParser = workbasketInputLayoutParser;
        this.workbasketLayoutParser = workbasketLayoutParser;
        this.searchInputLayoutParser = searchInputLayoutParser;
        this.searchResultLayoutParser = searchResultLayoutParser;
        this.caseTypeTabParser = caseTypeTabParser;
        this.wizardPageParser = wizardPageParser;
        this.searchCasesResultLayoutParser = searchCasesResultLayoutParser;
    }

    public ParseResult<GenericLayoutEntity> parseWorkbasketInputLayout(Map<String, DefinitionSheet> definitionSheets) {
        return workbasketInputLayoutParser.parseAll(definitionSheets);
    }

    public ParseResult<GenericLayoutEntity> parseWorkbasketLayout(Map<String, DefinitionSheet> definitionSheets) {
        return workbasketLayoutParser.parseAll(definitionSheets);
    }

    public ParseResult<GenericLayoutEntity> parseSearchCasesResultsLayout(
        Map<String, DefinitionSheet> definitionSheets) {
        return searchCasesResultLayoutParser.parseAllForSearchCases(definitionSheets);
    }

    public ParseResult<GenericLayoutEntity> parseSearchInputLayout(Map<String, DefinitionSheet> definitionSheets) {
        return searchInputLayoutParser.parseAll(definitionSheets);
    }

    public ParseResult<GenericLayoutEntity> parseSearchResultLayout(Map<String, DefinitionSheet> definitionSheets) {
        return searchResultLayoutParser.parseAll(definitionSheets);
    }

    public ParseResult<DisplayGroupEntity> parseAllDisplayGroups(Map<String, DefinitionSheet> definitionSheets) {
        return new ParseResult<DisplayGroupEntity>()
            .add(caseTypeTabParser.parseAll(definitionSheets))
            .add(wizardPageParser.parseAll(definitionSheets));
    }
}
