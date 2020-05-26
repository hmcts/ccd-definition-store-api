package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.*;
import org.mockito.*;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SearchCasesResultLayoutParser Tests")
public class SearchCasesResultLayoutParserTest {

    @Mock
    private ParseContext parseContext;

    @Mock
    private ShowConditionParser showConditionParser;

    private SearchCasesResultLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        definitionSheets = new HashMap<>();
        classUnderTest = new SearchCasesResultLayoutParser(parseContext, new EntityToDefinitionDataItemRegistry(), showConditionParser);
    }

    @Test
    @DisplayName("Should return name")
    public void shouldReturnNameWhenAsked() {
        assertThat(classUnderTest.getLayoutName(), is("searchCase"));
    }

    @Test
    @DisplayName("Should Fail when no worksheet provided")
    public void shouldThrowExceptionWhenWorkbasketInputWorksheetIsNotProvided() {
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.getDefinitionSheet(definitionSheets));
        assertEquals("A definition must contain a SearchCasesResultFields sheet", thrown.getMessage());
    }
}
