package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.*;
import org.mockito.*;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchCasesResultLayoutParser Tests")
public class UseCaseParserTest {

    @Mock
    private ParseContext parseContext;

    private UseCaseParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        definitionSheets = new HashMap<>();
        classUnderTest = new UseCaseParser(parseContext);
    }

    @Test
    @DisplayName("Should Pass when all data in definition is correct")
    public void shouldReturnWithNoErrors() {
        final DefinitionSheet sheetSCRF = addDefinitionSheet(SheetName.SEARCH_CASES_RESULT_FIELDS);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.SEARCH_CASES_RESULT_FIELDS.getName());
        definitionDataItem.addAttribute(ColumnName.USE_CASE, "ORGCASES");
        definitionDataItem.addAttribute(ColumnName.CASE_TYPE_ID, "caseTypeId");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        sheetSCRF.addDataItem(definitionDataItem);

        classUnderTest.parse(definitionSheets);
    }

    @Test
    @DisplayName("Should Pass when no data items provided")
    public void shouldReturnWithNoerrorsAndNoDataItems() {
        final DefinitionSheet sheetSCRF = addDefinitionSheet(SheetName.SEARCH_CASES_RESULT_FIELDS);

        classUnderTest.parse(definitionSheets);
    }

    @Test
    @DisplayName("Should Fail when incorrect useCase provided")
    public void shouldThrowExceptionWhenNoWorksheetIsNotProvided() {
        final DefinitionSheet sheetSCRF = addDefinitionSheet(SheetName.SEARCH_CASES_RESULT_FIELDS);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.SEARCH_CASES_RESULT_FIELDS.getName());
        definitionDataItem.addAttribute(ColumnName.USE_CASE, "WORKBASKET");
        definitionDataItem.addAttribute(ColumnName.CASE_TYPE_ID, "caseTypeId");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        sheetSCRF.addDataItem(definitionDataItem);

        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.parse(definitionSheets));
        assertEquals("Unsupported useCase parameter type 'WORKBASKET' for field 'fieldId' on tab 'SearchCasesResultFields'", thrown.getMessage());
    }

    private DefinitionSheet addDefinitionSheet(SheetName sheetName) {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(sheetName.toString());
        definitionSheets.put(sheetName.getName(), sheet);
        return sheet;
    }
}
