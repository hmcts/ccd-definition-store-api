package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
class BannerParserTest extends ParserTestBase {

    private BannerParser bannerParser;

    @Mock
    private JurisdictionEntity jurisdiction;


    @BeforeEach
    public void setup() {

        init();
        parseContext = mock(ParseContext.class);
        bannerParser = new BannerParser(parseContext);

        definitionSheets.put(SheetName.BANNER.getName(), definitionSheet);
        given(parseContext.getJurisdiction()).willReturn(jurisdiction);
    }

    @Test
    public void shouldFail_whenMorethanOneBannerDefinedForJurisdiction() {
        definitionSheet.addDataItem(buildDefinitionDataItem("Test Desc1", "ULR Text2", "http://localhost:3451/test2"));
        definitionSheet.addDataItem(buildDefinitionDataItem("Test Desc2", "ULR Text2", "http://localhost:3451/test2"));
        Assertions.assertThrows(SpreadsheetParsingException.class, () -> bannerParser.parse(definitionSheets));
    }

    @Test
    public void shouldParse() {
        definitionSheet.addDataItem(buildDefinitionDataItem("Test Desc1", "ULR Text2", "http://localhost:3451/test2"));
        bannerParser.parse(definitionSheets);
    }

    private DefinitionDataItem buildDefinitionDataItem(String description, String urlText, String url) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.BANNER.toString());
        item.addAttribute(ColumnName.BANNER_DESCRIPTION.toString(), description);
        item.addAttribute(ColumnName.BANNER_ENABLED.toString(), true);
        item.addAttribute(ColumnName.BANNER_URL.toString(), url);
        item.addAttribute(ColumnName.BANNER_URL_TEXT.toString(), urlText);
        return item;
    }

}
