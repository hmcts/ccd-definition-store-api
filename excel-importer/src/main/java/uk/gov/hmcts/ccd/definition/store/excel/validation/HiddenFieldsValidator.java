package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.Map;
import java.util.Optional;

public class HiddenFieldsValidator {

    public Boolean parseHiddenFields(DefinitionDataItem definitionDataItem, Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionSheet caseEventToFields = definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName());
        final DefinitionSheet caseFields = definitionSheets.get(SheetName.CASE_FIELD.getName());
        Optional<DefinitionDataItem> caseField =
            caseFields.getDataItems().stream().filter(caseFieldDataItem ->
                definitionDataItem.getId().equals(caseFieldDataItem.getString(ColumnName.FIELD_TYPE))).findFirst();
        caseField.ifPresent(ddi -> {
            Optional<DefinitionDataItem> caseEventToField = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> ddi.getId().equals(definitionDataItem1.getCaseFieldId())).findFirst();
            caseEventToField.ifPresent(caseEventToFieldDataItem -> {
                String fieldShowCondition = caseEventToFieldDataItem.getString(ColumnName.FIELD_SHOW_CONDITION);
                Boolean caseFieldRetainHiddenValue = caseEventToFieldDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE);
                if (fieldShowCondition == null && Boolean.TRUE.equals(definitionDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE))) {
                    //caseEventToField FieldShowCondition is null and complexType ListElementCode retainHiddenValue is true
                    throw new MapperException(String.format("retainHiddenValue can only be configured "
                            + "for a field that uses a "
                        + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                        caseEventToFieldDataItem.getCaseFieldId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
                } else if (Boolean.FALSE.equals(caseFieldRetainHiddenValue)
                    && Boolean.TRUE.equals(definitionDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE))) {
                    //caseEventToField retainHiddenValue is false and complexType ListElementCode retainHiddenValue true
                    throw new MapperException(String.format("retainHiddenValue' has been incorrectly configured or is invalid for fieldID ['%s'] on ['%s']",
                        caseEventToFieldDataItem.getCaseFieldId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
                }
            });
        });
        return definitionDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE);
    }

    public Boolean parseHiddenFields(DefinitionDataItem definitionDataItem) {
        if (definitionDataItem.getString(ColumnName.FIELD_SHOW_CONDITION) == null
            && Boolean.TRUE.equals(definitionDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE))) {
            throw new MapperException(String.format("retainHiddenValue can only be configured for a field that uses a "
                    + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                definitionDataItem.getString(ColumnName.CASE_FIELD_ID), SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
        return definitionDataItem.getBoolean(ColumnName.RETAIN_HIDDEN_VALUE);
    }
}
