package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;

@DisplayName("WorkbasketInputLayoutParser Tests")
public class WorkbasketInputLayoutParserTest {

    @Mock
    private ParseContext parseContext;

    private WorkbasketInputLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        definitionSheets = new HashMap<>();
        classUnderTest = new WorkbasketInputLayoutParser(parseContext, new EntityToDefinitionDataItemRegistry());
    }

    @Test(expected = MapperException.class)
    @DisplayName("Should Fail when no worksheet provided")
    public void shouldThrowExceptionWhenWorkbasketInputWorksheetIsNotProvided() {
        classUnderTest.getDefinitionSheet(definitionSheets);
    }

    @Test
    @DisplayName("Should now fail when no values provided in WorkbasketInputFields worksheet")
    public void shouldNotFailWhenNoDataInWorksheet() {
        final DefinitionSheet sheet = new DefinitionSheet();
        definitionSheets.put(WORK_BASKET_INPUT_FIELD.getName(), sheet);
        classUnderTest.parseAll(definitionSheets);
    }

    @Test
    @DisplayName("Should return name")
    public void shouldReturnNameWhenAsked() {
        assertThat(classUnderTest.getLayoutName(), is(WORK_BASKET_INPUT_FIELD.getName()));
    }
}
