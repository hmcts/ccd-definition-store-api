package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.SEARCH_INPUT_FIELD;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@DisplayName("SearchInputLayoutParser Tests")
public class SearchInputLayoutParserTest {
    private static final String CASE_TYPE_ID = "Valid Case Type";
    private static final String CASE_FIELD_ID = "Some Field";

    private SearchInputLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_ID);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(CASE_FIELD_ID);

        final ParseContext parseContext = new ParseContext();
        parseContext.registerCaseType(caseTypeEntity);
        parseContext.registerCaseFieldForCaseType(CASE_TYPE_ID, caseFieldEntity);

        definitionSheets = new HashMap<>();
        classUnderTest = new SearchInputLayoutParser(parseContext, new EntityToDefinitionDataItemRegistry());
    }

    @Test(expected = MapperException.class)
    @DisplayName("Should Fail when no worksheet provided")
    public void shouldThrowExceptionWhenWorkbasketInputWorksheetIsNotProvided() {
        classUnderTest.getDefinitionSheet(definitionSheets);
    }

    @Test(expected = SpreadsheetParsingException.class)
    @DisplayName("Should fail when no values provided in SearchInputFields worksheet")
    public void shouldFailWhenNoDataInWorksheet() {
        final DefinitionSheet sheet = new DefinitionSheet();
        definitionSheets.put(SEARCH_INPUT_FIELD.getName(), sheet);
        classUnderTest.parseAll(definitionSheets);
    }

    @Test
    @DisplayName("Should return name")
    public void shouldReturnNameWhenAsked() {
        assertThat(classUnderTest.getLayoutName(), is("Search Inputs"));
    }
}
