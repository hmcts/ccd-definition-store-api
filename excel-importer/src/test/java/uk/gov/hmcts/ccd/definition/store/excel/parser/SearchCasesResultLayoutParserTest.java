package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SearchCasesResultLayoutParser Tests")
class SearchCasesResultLayoutParserTest {

    @Mock
    private ParseContext parseContext;

    @Mock
    private ShowConditionParser showConditionParser;

    private SearchCasesResultLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        definitionSheets = new HashMap<>();
        classUnderTest = new SearchCasesResultLayoutParser(
            parseContext, new EntityToDefinitionDataItemRegistry(), showConditionParser);
    }

    @Test
    @DisplayName("Should return name")
    void shouldReturnNameWhenAsked() {
        assertThat(classUnderTest.getLayoutName(), is("searchCase"));
    }

    @Test
    @DisplayName("Should Fail when no worksheet provided")
    void shouldThrowExceptionWhenWorkbasketInputWorksheetIsNotProvided() {
        MapperException thrown = assertThrows(
            MapperException.class, () -> classUnderTest.getDefinitionSheet(definitionSheets));
        assertEquals("A definition must contain a SearchCasesResultFields sheet", thrown.getMessage());
    }

    @Test
    @DisplayName("Should fail when populateShowConditon is invoked")
    void shouldThrowExceptionWhenPopulateShowConditionIsInvoked() {
        GenericLayoutEntity layoutEntity = new SearchInputCaseFieldEntity();
        layoutEntity.setCaseType(new CaseTypeEntity());
        MapperException thrown = assertThrows(
            MapperException.class, () -> classUnderTest.populateShowCondition(layoutEntity, "WORKBASKET"));
        assertEquals(String.format("showCondition is not supported in worksheet '%s' for caseType '%s'",
            SheetName.SEARCH_CASES_RESULT_FIELDS.getName(),
            layoutEntity.getCaseType().getReference()),
            thrown.getMessage());
    }
}
