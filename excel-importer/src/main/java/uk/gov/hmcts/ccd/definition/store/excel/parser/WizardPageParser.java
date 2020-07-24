package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.Optional;

public class WizardPageParser extends AbstractDisplayGroupParser {

    public WizardPageParser(ParseContext parseContext,
                            ShowConditionParser showConditionParser,
                            EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        super(parseContext, showConditionParser, entityToDefinitionDataItemRegistry);
        this.displayGroupId = ColumnName.PAGE_ID;
        this.displayGroupLabel = ColumnName.PAGE_LABEL;
        this.displayGroupOrder = ColumnName.PAGE_DISPLAY_ORDER;
        this.displayGroupFieldDisplayOrder = ColumnName.PAGE_FIELD_DISPLAY_ORDER;
        this.displayGroupPurpose = DisplayGroupPurpose.EDIT;
        this.displayGroupType = DisplayGroupType.PAGE;
        this.displayGroupItemMandatory = false;
        this.sheetName = SheetName.CASE_EVENT_TO_FIELDS;
        this.groupShowConditionColumn = Optional.of(ColumnName.PAGE_SHOW_CONDITION);
        this.columnId = Optional.of(ColumnName.PAGE_COLUMN);
    }
}
