package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class HiddenFieldsValidator {
    Boolean retainHiddenValue;

    public Boolean parseComplexTypesHiddenFields(DefinitionDataItem definitionDataItem,
                                                 Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionSheet caseEventToFields = definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName());
        final DefinitionSheet caseFields = definitionSheets.get(SheetName.CASE_FIELD.getName());
        List<DefinitionDataItem> caseEventToFieldsList =
            caseFields.getDataItems().stream().filter(caseFieldDataItem ->
                definitionDataItem.getId().equals(caseFieldDataItem
                    .getString(ColumnName.FIELD_TYPE))
                    || definitionDataItem.getId().equals(caseFieldDataItem
                    .getString(ColumnName.FIELD_TYPE_PARAMETER))).collect(toList());

        caseEventToFieldsList.forEach(ddi -> {
            Optional<DefinitionDataItem> caseEventToField = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> ddi.getId()
                    .equals(definitionDataItem1.getCaseFieldId())).findFirst();
            caseEventToField.ifPresent(caseEventToFieldDataItem -> {
                Boolean caseFieldRetainHiddenValue = caseEventToFieldDataItem.getRetainHiddenValue();
                if (isSubFieldsIncorrectlyConfigured(caseFieldRetainHiddenValue, definitionDataItem)) {
                    throw new MapperException(String.format(
                        "'retainHiddenValue' has been incorrectly configured or is invalid for "
                            + "fieldID ['%s'] on ['%s']",
                        caseEventToFieldDataItem.getCaseFieldId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
                }
                retainHiddenValue = definitionDataItem.getRetainHiddenValue();
            });
        });

        validateCaseEventToFields(definitionDataItem, caseEventToFields, caseEventToFieldsList);

        return definitionDataItem.getRetainHiddenValue();
    }

    private void validateCaseEventToFields(DefinitionDataItem definitionDataItem,
                                           DefinitionSheet caseEventToFields,
                                           List<DefinitionDataItem> caseField) {
        boolean valid = true;
        String caseFieldId = null;
        for (DefinitionDataItem cf : caseField) {
            List<DefinitionDataItem> caseEventToFieldList = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> cf.getId()
                    .equals(definitionDataItem1.getCaseFieldId())).collect(toList());
            caseFieldId = cf.getId();
            valid = isAtLeastOneCaseEventToFieldsConfigured(caseEventToFieldList, definitionDataItem);
            if (valid) {
                break;
            }
        }
        if (definitionDataItem.getRetainHiddenValue() != null && !valid) {
            throw new MapperException(String.format("'retainHiddenValue' can only be configured "
                    + "for a field that uses a "
                    + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                caseFieldId, SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
    }

    private boolean isAtLeastOneCaseEventToFieldsConfigured(List<DefinitionDataItem> caseEventToFieldList, DefinitionDataItem definitionDataItem) {
        boolean match;
        if (definitionDataItem.getRetainHiddenValue() != null && definitionDataItem.getFieldShowCondition() != null) {
            match = true;
        } else if (definitionDataItem.getRetainHiddenValue() != null && definitionDataItem.getFieldShowCondition() == null) {
            List<DefinitionDataItem> caseEventToFieldListFiltered =
                caseEventToFieldList.stream().filter(dataItem -> dataItem.getString(ColumnName.FIELD_SHOW_CONDITION) != null).collect(toList());
            if (Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())) {
                match = caseEventToFieldListFiltered.stream().noneMatch(dataItem -> Boolean.FALSE.equals(dataItem.getRetainHiddenValue())
                    || dataItem.getRetainHiddenValue() == null);
            }
            else {
                match = true;
            }
        } else {
            match = true;
        }
        return match;
    }

    private boolean isShowConditionNull(String fieldShowCondition, DefinitionDataItem definitionDataItem) {
        return (fieldShowCondition == null && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
    }

    private boolean isSubFieldsIncorrectlyConfigured(Boolean caseFieldRetainHiddenValue,
                                                     DefinitionDataItem definitionDataItem) {
        if (definitionDataItem.getRetainHiddenValue() != null && definitionDataItem.getFieldShowCondition() != null) {
           return Boolean.FALSE.equals(caseFieldRetainHiddenValue) ? true : false;
        } else {
            return (Boolean.FALSE.equals(caseFieldRetainHiddenValue)
                && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
        }
    }

    public Boolean parseHiddenFields(DefinitionDataItem definitionDataItem) {
        if (isShowConditionNull(definitionDataItem.getString(ColumnName.FIELD_SHOW_CONDITION), definitionDataItem)) {
            throw new MapperException(String.format(
                "'retainHiddenValue' can only be configured for a field that uses a "
                    + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                definitionDataItem.getString(ColumnName.CASE_FIELD_ID), SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
        return definitionDataItem.getRetainHiddenValue();
    }
}
