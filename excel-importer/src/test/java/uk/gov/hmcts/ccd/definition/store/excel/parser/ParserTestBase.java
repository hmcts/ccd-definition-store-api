package uk.gov.hmcts.ccd.definition.store.excel.parser;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.fail;

abstract class ParserTestBase {

    protected static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    protected static final String CASE_FIELD_UNDER_TEST = "Some Case Field";
    protected static final String COMPLEX_FIELD_UNDER_TEST = "Some Complex Field";
    protected ParseContext parseContext;
    protected CaseTypeEntity caseType;
    protected Map<String, DefinitionSheet> definitionSheets;
    protected DefinitionSheet definitionSheet;

    protected void init() {
        definitionSheets = new LinkedHashMap<>();
        definitionSheet = new DefinitionSheet();
    }

    protected void assertLogged(final ArgumentCaptor<LoggingEvent> captorLoggingEvent, final String message) {
        if (!captorLoggingEvent.getAllValues()
            .stream()
            .anyMatch(e -> e.getFormattedMessage().equals(message))) {
            fail("No event matches " + message);
        }
    }
}
