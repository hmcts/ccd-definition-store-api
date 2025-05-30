package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BannerParserTest extends ParserTestBase {

    private static final String BANNER_DESCRIPTION = "Test Desc1";
    private static final String BANNER_URL = "http://localhost:3451/test2";
    private static final String BANNER_URL_TEXT = "URL Text2";

    private BannerParser bannerParser;

    @BeforeEach
    void setup() {

        init();
        parseContext = mock(ParseContext.class);
        bannerParser = new BannerParser(parseContext);

        definitionSheets.put(SheetName.BANNER.getName(), definitionSheet);
    }

    @Test
    void shouldFail_whenMoreThanOneBannerDefinedForJurisdiction() {
        definitionSheet.addDataItem(buildDefinitionDataItem(BANNER_DESCRIPTION, BANNER_URL_TEXT, BANNER_URL));
        definitionSheet.addDataItem(buildDefinitionDataItem("Test Desc2", BANNER_URL_TEXT, BANNER_URL));
        Assertions.assertThrows(SpreadsheetParsingException.class, () -> bannerParser.parse(definitionSheets));
    }

    @Test
    void shouldParse() {
        definitionSheet.addDataItem(buildDefinitionDataItem(BANNER_DESCRIPTION, BANNER_URL_TEXT, BANNER_URL));

        Optional<BannerEntity> bannerEntity = bannerParser.parse(definitionSheets);

        assertAll(
            () -> assertThat(bannerEntity.isPresent(), is(true)),
            () -> assertThat(bannerEntity.get().getBannerDescription(), is(BANNER_DESCRIPTION)),
            () -> assertThat(bannerEntity.get().getBannerUrlText(), is(BANNER_URL_TEXT)),
            () -> assertThat(bannerEntity.get().getBannerUrl(), is(BANNER_URL)),
            () -> assertThat(bannerEntity.get().getBannerEnabled(), is(true))
        );
    }

    @Test
    void shouldReturnEmptyOptionalWhenBannerSheetHasNoItems() {
        Optional<BannerEntity> bannerEntity = bannerParser.parse(definitionSheets);

        assertAll(
            () -> assertThat(bannerEntity.isPresent(), is(false))
        );
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
