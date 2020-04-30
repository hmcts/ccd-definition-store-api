package uk.gov.hmcts.ccd.definition.store.excel.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;

@RunWith(MockitoJUnitRunner.class)
class JurisdictionUiConfigParserTest extends ParserTestBase {

    private JurisdictionUiConfigParser parser;

    @Mock
    private JurisdictionEntity jurisdiction;

    private static final String TRUE_STRING = "Yes";

    @BeforeEach
    public void setup() {
        init();

        parseContext = mock(ParseContext.class);
        parser = new JurisdictionUiConfigParser(parseContext);

        definitionSheets.put(SheetName.JURISDICTION.getName(), definitionSheet);
        given(parseContext.getJurisdiction()).willReturn(jurisdiction);
    }

    @Test
    public void shouldParse() {
        definitionSheet.addDataItem(buildDefinitionDataItem(TRUE_STRING));
        JurisdictionUiConfigEntity result = parser.parse(definitionSheets);
        assertAll(() -> assertEquals(true, result.getShuttered()),
            () -> assertEquals(jurisdiction, result.getJurisdiction()));
    }

    @Test
    public void shouldParse_WhenShutteredIsNull() {
        definitionSheet.addDataItem(buildDefinitionDataItem(null));
        JurisdictionUiConfigEntity result = parser.parse(definitionSheets);
        assertAll(() -> assertEquals(false, result.getShuttered()),
            () -> assertEquals(jurisdiction, result.getJurisdiction()));
    }

    private DefinitionDataItem buildDefinitionDataItem(String shuttered) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.JURISDICTION.toString());
        item.addAttribute(ColumnName.SHUTTERED.toString(), shuttered);
        return item;
    }

}
