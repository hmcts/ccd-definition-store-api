package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Map;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

public class JurisdictionParser {

    public JurisdictionEntity parse(Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionDataItem jurisdictionItem = definitionSheets.get(SheetName.JURISDICTION.getName()).getDataItems().get(0);

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(jurisdictionItem.getString(ColumnName.ID));
        jurisdiction.setName(jurisdictionItem.getString(ColumnName.NAME));
        jurisdiction.setDescription(jurisdictionItem.getString(ColumnName.DESCRIPTION));
        jurisdiction.setLiveFrom(jurisdictionItem.getDate(ColumnName.LIVE_FROM));
        jurisdiction.setLiveTo(jurisdictionItem.getDate(ColumnName.LIVE_TO));
        jurisdiction.setBannerEnabled(jurisdictionItem.getBoolean(ColumnName.BANNER_ENABLED));
        jurisdiction.setBannerDescription(jurisdictionItem.getString(ColumnName.BANNER_DESCRIPTION));
        jurisdiction.setBannerUrlText(jurisdictionItem.getString(ColumnName.BANNER_URL_TEXT));
        jurisdiction.setBannerUrl(jurisdictionItem.getString(ColumnName.BANNER_URL));

        return jurisdiction;
    }
}
