package uk.gov.hmcts.ccd.definition.store.excel.parser;


import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImplTest;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SpreadsheetParserTest {

    private SpreadsheetParser spreadsheetParser;

    @Mock
    private SpreadsheetValidator spreadsheetValidator;

    @BeforeEach
    public void setup() {
        spreadsheetParser = new SpreadsheetParser(spreadsheetValidator);
    }

    @Test
    public void shouldParse() throws Exception {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(ImportServiceImplTest.BAD_FILE);

        final Map<String, DefinitionSheet> map = spreadsheetParser.parse(inputStream);
        assertThat(map.size(), is(18));

        assertThat(map.keySet(), containsInAnyOrder("SearchInputFields", "UserProfile", "CaseField",
            "ComplexTypes", "WorkBasketResultFields", "CaseTypeTab",
            "FixedLists", "CaseEvent", "Jurisdiction",
            "SearchResultFields", "AuthorisationCaseField", "CaseType", "State",
            "AuthorisationCaseType", "AuthorisationComplexType",
            "AuthorisationCaseEvent", "CaseEventFieldRestriction", "Banner"
        ));

        final List<String> importWarnings = spreadsheetParser.getImportWarnings();
        assertThat(importWarnings.size(), is(2));

        assertThat(importWarnings, containsInAnyOrder(
            "CaseField sheet contains DefaultHidden column that will be deprecated. "
                + "Please remove from future Definition imports.",
            "ComplexTypes sheet contains DefaultHidden column that will be deprecated. "
                + "Please remove from future Definition imports."
        ));
    }

    /**
     * Helps test coverage.
     */
    @Test
    public void shouldFail_whenInvalidInputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            final InputStream inputStream = null;
            spreadsheetParser.parse(inputStream);
        });
    }
}
