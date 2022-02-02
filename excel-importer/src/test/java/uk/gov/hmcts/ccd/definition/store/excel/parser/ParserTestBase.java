package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;


abstract class ParserTestBase {

    protected static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    protected static final String CASE_FIELD_UNDER_TEST = "Some Case Field";
    protected static final String CASE_EVENT_UNDER_TEST = "Are we there yet";
    protected static final String COMPLEX_FIELD_UNDER_TEST = "Some Complex Field";

    protected ParseContext parseContext;
    protected CaseTypeEntity caseType;
    protected Map<String, DefinitionSheet> definitionSheets;
    protected DefinitionSheet definitionSheet;

    protected void init() {
        definitionSheets = new LinkedHashMap<>();
        definitionSheet = new DefinitionSheet();
    }

}
