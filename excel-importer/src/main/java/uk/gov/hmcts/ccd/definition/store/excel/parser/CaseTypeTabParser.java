package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.Optional;

public class CaseTypeTabParser extends AbstractDisplayGroupParser {

    public CaseTypeTabParser(ParseContext parseContext,
                             ShowConditionParser showConditionParser,
                             EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        super(parseContext, showConditionParser, entityToDefinitionDataItemRegistry);
        this.displayGroupId = ColumnName.TAB_ID;
        this.displayGroupLabel = ColumnName.TAB_LABEL;
        this.displayGroupOrder = ColumnName.TAB_DISPLAY_ORDER;
        this.displayGroupFieldDisplayOrder = ColumnName.TAB_FIELD_DISPLAY_ORDER;
        this.displayGroupPurpose = DisplayGroupPurpose.VIEW;
        this.displayGroupType = DisplayGroupType.TAB;
        this.displayGroupItemMandatory = true;
        this.groupShowConditionColumn = Optional.of(ColumnName.TAB_SHOW_CONDITION);
        this.fieldShowConditionColumn = Optional.of(ColumnName.FIELD_SHOW_CONDITION);
        this.sheetName = SheetName.CASE_TYPE_TAB;
        this.userRoleColumn = Optional.of(ColumnName.USER_ROLE);
    }
}
