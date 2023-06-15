package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;


import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.FIELD_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LABEL;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

public class TranslationHelper {
    protected static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    protected static final String CASE_EVENT_UNDER_TEST = "Are we there yet";
    protected static final String CASE_FIELD_UNDER_TEST = "Text";
    protected static final String YES_OR_NO = "YesOrNo";
    protected static final String CASE_TYPE_ID = "N>G>I>T>B.";
    protected static final String CASE_FIELD_ID = "BaYaN";

    private TranslationHelper() {
    }

    public static DefinitionSheet buildSheetForCaseEvent() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_EVENT.getName());
        final DefinitionDataItem item = new DefinitionDataItem(CASE_EVENT.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.ID, CASE_EVENT_UNDER_TEST);
        item.addAttribute(ColumnName.NAME, CASE_EVENT_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }

    public static DefinitionSheet buildSheetForGenerics(final SheetName sheetName) {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(sheetName.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        return sheet;
    }

    static DefinitionSheet buildSheetForCaseType() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_TYPE.getName());
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        item.addAttribute(ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(NAME, CASE_TYPE_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }

    static DefinitionSheet buildSheetForCaseField() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_FIELD.getName());

        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_FIELD.getName());
        item.addAttribute(ID, CASE_FIELD_ID);
        item.addAttribute(LABEL, CASE_FIELD_UNDER_TEST);
        item.addAttribute(FIELD_TYPE, CASE_FIELD_UNDER_TEST);
        sheet.addDataItem(item);

        final DefinitionDataItem yesNoItem = new DefinitionDataItem(CASE_FIELD.getName());
        yesNoItem.addAttribute(ID, YES_OR_NO);
        yesNoItem.addAttribute(FIELD_TYPE, YES_OR_NO);
        yesNoItem.addAttribute(LABEL, YES_OR_NO);
        sheet.addDataItem(yesNoItem);

        return sheet;
    }
}
