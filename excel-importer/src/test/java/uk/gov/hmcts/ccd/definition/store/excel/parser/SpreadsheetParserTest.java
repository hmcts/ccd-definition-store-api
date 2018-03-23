package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImplTest;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetParserTest {

    private SpreadsheetParser spreadsheetParser;

    @Mock
    private SpreadsheetValidator spreadsheetValidator;

    @Before
    public void setup() {
        spreadsheetParser = new SpreadsheetParser(spreadsheetValidator);
    }

    @Test
    public void shouldParse() throws Exception {
        final InputStream inputStream = ClassLoader.getSystemResourceAsStream(ImportServiceImplTest.BAD_FILE);

        final Map<String, DefinitionSheet> map = spreadsheetParser.parse(inputStream);
        assertThat(map.size(), is(16));

        assertThat(map.keySet(), containsInAnyOrder("SearchInputFields", "UserProfile", "CaseField",
            "ComplexTypes", "WorkBasketResultFields", "CaseTypeTab", "FixedLists", "CaseEvent", "Jurisdiction",
            "SearchResultFields", "AuthorisationCaseField", "CaseType", "State", "AuthorisationCaseType",
            "AuthorisationCaseEvent", "CaseEventFieldRestriction"
        ));
    }

    /**
     * Helps test coverage.
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void shouldFail_whenInvalidInputStream() throws Exception {
        spreadsheetParser.parse(null);
    }
}
