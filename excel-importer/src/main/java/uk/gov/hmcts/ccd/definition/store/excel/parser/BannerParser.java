package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BannerParser {

    private final ParseContext parseContext;

    public BannerParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public Optional<BannerEntity> parse(Map<String, DefinitionSheet> definitionSheets) {
        List<DefinitionDataItem> bannerItems = definitionSheets.get(SheetName.BANNER.getName()).getDataItems();
        if (bannerItems.isEmpty()) {
            return Optional.empty();
        }
        if (bannerItems.size() > 1) {
            throw new SpreadsheetParsingException("Multiple Banners not allowed for Jurisdiction");
        }
        final DefinitionDataItem bannerItem = bannerItems.get(0);

        BannerEntity banner = new BannerEntity();
        banner.setBannerDescription(bannerItem.getString(ColumnName.BANNER_DESCRIPTION));
        banner.setBannerEnabled(bannerItem.getBoolean(ColumnName.BANNER_ENABLED));
        banner.setBannerUrl(bannerItem.getString(ColumnName.BANNER_URL));
        banner.setBannerUrlText(bannerItem.getString(ColumnName.BANNER_URL_TEXT));
        banner.setJurisdiction(parseContext.getJurisdiction());

        return Optional.of(banner);
    }
}
