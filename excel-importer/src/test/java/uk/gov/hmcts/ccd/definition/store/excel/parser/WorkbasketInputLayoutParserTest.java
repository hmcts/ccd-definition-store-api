package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;

@DisplayName("WorkbasketInputLayoutParser Tests")
public class WorkbasketInputLayoutParserTest {

    @Mock
    private ParseContext parseContext;

    private WorkbasketInputLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        definitionSheets = new HashMap<>();
        classUnderTest = new WorkbasketInputLayoutParser(parseContext, new EntityToDefinitionDataItemRegistry());
    }

    @Test
    @DisplayName("Should Fail when no worksheet provided")
    public void shouldThrowExceptionWhenWorkbasketInputWorksheetIsNotProvided() {
        MapperException thrown = assertThrows(MapperException.class, () -> classUnderTest.getDefinitionSheet(definitionSheets));
        assertEquals("A definition must contain a WorkBasketInputFields sheet", thrown.getMessage());
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
