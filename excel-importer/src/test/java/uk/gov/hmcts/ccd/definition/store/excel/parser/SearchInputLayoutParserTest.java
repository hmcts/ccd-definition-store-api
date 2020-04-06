package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

@DisplayName("SearchInputLayoutParser Tests")
public class SearchInputLayoutParserTest {
    private static final String CASE_TYPE_ID = "Valid Case Type";
    private static final String CASE_FIELD_ID = "Some Field";

    private SearchInputLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @Mock
    private ShowConditionParser showConditionParser;

    @BeforeEach
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
        classUnderTest = new SearchInputLayoutParser(parseContext, new EntityToDefinitionDataItemRegistry(), showConditionParser);
    }

    @Test
    @DisplayName("Should Fail when no worksheet provided")
    public void shouldThrowExceptionWhenWorkbasketInputWorksheetIsNotProvided() {
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.getDefinitionSheet(definitionSheets));
        assertEquals("A definition must contain a SearchInputFields sheet", thrown.getMessage());
    }

    @Test
    @DisplayName("Should fail when no values provided in SearchInputFields worksheet")
    public void shouldFailWhenNoDataInWorksheet() {
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.getDefinitionSheet(definitionSheets));
        assertEquals("A definition must contain a SearchInputFields sheet", thrown.getMessage());
    }

    @Test
    @DisplayName("Should return name")
    public void shouldReturnNameWhenAsked() {
        assertThat(classUnderTest.getLayoutName(), is("Search Inputs"));
    }

    @Test
    @DisplayName("Should fail when populateSortOrder is invoked")
    public void shouldThrowExceptionWhenPopulateSortOrderIsInvoked() {
        GenericLayoutEntity layoutEntity = new SearchInputCaseFieldEntity();
        layoutEntity.setCaseType(new CaseTypeEntity());
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.populateSortOrder(layoutEntity, "1:ASC"));
        assertEquals(String.format("Results ordering is not supported in worksheet '%s' for caseType '%s'",
            SheetName.SEARCH_INPUT_FIELD.getName(), layoutEntity.getCaseType().getReference()), thrown.getMessage());
    }
}
