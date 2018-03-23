package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.Map;

public class LayoutParser {

    private final WorkbasketInputLayoutParser workbasketInputLayoutParser;
    private final WorkbasketLayoutParser workbasketLayoutParser;
    private final SearchInputLayoutParser searchInputLayoutParser;
    private final SearchResultLayoutParser searchResultLayoutParser;
    private final CaseTypeTabParser caseTypeTabParser;
    private final WizardPageParser wizardPageParser;

    public LayoutParser(WorkbasketInputLayoutParser workbasketInputLayoutParser,
                        WorkbasketLayoutParser workbasketLayoutParser,
                        SearchInputLayoutParser searchInputLayoutParser,
                        SearchResultLayoutParser searchResultLayoutParser,
                        CaseTypeTabParser caseTypeTabParser,
                        WizardPageParser wizardPageParser) {
        this.workbasketInputLayoutParser = workbasketInputLayoutParser;
        this.workbasketLayoutParser = workbasketLayoutParser;
        this.searchInputLayoutParser = searchInputLayoutParser;
        this.searchResultLayoutParser = searchResultLayoutParser;
        this.caseTypeTabParser = caseTypeTabParser;
        this.wizardPageParser = wizardPageParser;
    }

    public ParseResult<GenericLayoutEntity> parseAllGenerics(Map<String, DefinitionSheet> definitionSheets) {
        return new ParseResult<GenericLayoutEntity>()
            .add(workbasketInputLayoutParser.parseAll(definitionSheets))
            .add(workbasketLayoutParser.parseAll(definitionSheets))
            .add(searchInputLayoutParser.parseAll(definitionSheets))
            .add(searchResultLayoutParser.parseAll(definitionSheets));
    }

    public ParseResult<DisplayGroupEntity> parseAllDisplayGroups(Map<String, DefinitionSheet> definitionSheets) {
        return new ParseResult<DisplayGroupEntity>()
            .add(caseTypeTabParser.parseAll(definitionSheets))
            .add(wizardPageParser.parseAll(definitionSheets));
    }
}
