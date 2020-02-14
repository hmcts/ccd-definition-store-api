package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Map;
import java.util.Optional;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;

public class JurisdictionUiConfigParser {

	private final ParseContext parseContext;

    public JurisdictionUiConfigParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }
    
    public JurisdictionUiConfigEntity parse(Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionDataItem jurisdictionItem = definitionSheets.get(SheetName.JURISDICTION.getName()).getDataItems().get(0);

        JurisdictionUiConfigEntity jurisdictionUiConfig = new JurisdictionUiConfigEntity();
        jurisdictionUiConfig.setShuttered(defaultFalse(jurisdictionItem.getBoolean(ColumnName.SHUTTERED)));
        jurisdictionUiConfig.setJurisdiction(parseContext.getJurisdiction());

        return jurisdictionUiConfig;
    }

    private Boolean defaultFalse(Boolean value) {
    	return Optional.ofNullable(value).orElse(false);
    }
}
