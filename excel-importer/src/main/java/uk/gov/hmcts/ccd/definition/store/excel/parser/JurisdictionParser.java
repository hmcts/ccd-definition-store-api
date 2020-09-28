package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.Map;

public class JurisdictionParser {

    public JurisdictionEntity parse(Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionDataItem jurisdictionItem = definitionSheets
            .get(SheetName.JURISDICTION.getName()).getDataItems().get(0);

        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(jurisdictionItem.getString(ColumnName.ID));
        jurisdiction.setName(jurisdictionItem.getString(ColumnName.NAME));
        jurisdiction.setDescription(jurisdictionItem.getString(ColumnName.DESCRIPTION));
        jurisdiction.setLiveFrom(jurisdictionItem.getDate(ColumnName.LIVE_FROM));
        jurisdiction.setLiveTo(jurisdictionItem.getDate(ColumnName.LIVE_TO));

        return jurisdiction;
    }
}
