package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Map;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;

public class BannerParser {

    private final ParseContext parseContext;

    public BannerParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public BannerEntity parse(Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionDataItem jurisdictionItem = definitionSheets.get(SheetName.BANNER.getName()).getDataItems().get(0);

        BannerEntity banner = new BannerEntity();
        banner.setBannerDescription(jurisdictionItem.getString(ColumnName.BANNER_DESCRIPTION));
        banner.setBannerEnabled(jurisdictionItem.getBoolean(ColumnName.BANNER_ENABLED));
        banner.setBannerUrl(jurisdictionItem.getString(ColumnName.BANNER_URL));
        banner.setBannerUrlText(jurisdictionItem.getString(ColumnName.BANNER_URL_TEXT));
        banner.setJurisdiction(parseContext.getJurisdiction());

        return banner;
    }
}
