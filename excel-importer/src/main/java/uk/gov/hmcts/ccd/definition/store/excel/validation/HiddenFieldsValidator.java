package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class HiddenFieldsValidator {
    public Boolean parseComplexTypesHiddenFields(DefinitionDataItem definitionDataItem,
                                                 Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionSheet caseEventToFields = definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName());
        final DefinitionSheet caseFields = definitionSheets.get(SheetName.CASE_FIELD.getName());
        List<DefinitionDataItem> caseFieldList =
            caseFields.getDataItems().stream().filter(caseFieldDataItem ->
                definitionDataItem.getId().equals(caseFieldDataItem
                    .getString(ColumnName.FIELD_TYPE))
                    || definitionDataItem.getId().equals(caseFieldDataItem
                    .getString(ColumnName.FIELD_TYPE_PARAMETER))).collect(toList());

        List<DefinitionDataItem> caseEventToFieldListFiltered = new ArrayList<>();
        for (DefinitionDataItem cf : caseFieldList) {
            for (DefinitionDataItem cetf : caseEventToFields.getDataItems()) {
                if (cetf.getCaseFieldId().equals(cf.getId())) {
                    caseEventToFieldListFiltered.add(cetf);
                }
            }
        }

        validateCaseEventToFields(definitionDataItem, definitionSheets, caseFieldList, caseEventToFieldListFiltered);
        validateSubFieldConfiguration(caseFieldList, definitionDataItem, caseEventToFields);

        return definitionDataItem.getRetainHiddenValue();
    }

    private void validateSubFieldConfiguration(List<DefinitionDataItem> caseField,
                                               DefinitionDataItem definitionDataItem,
                                               DefinitionSheet caseEventToFields) {

        boolean valid = false;
        String caseFieldId = null;
        for (DefinitionDataItem cf : caseField) {
            List<DefinitionDataItem> caseEventToFieldList = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> cf.getId()
                    .equals(definitionDataItem1.getCaseFieldId())).collect(toList());
            caseFieldId = cf.getId();
            valid = isAtLeastOneCaseEventToFieldsConfigured(caseEventToFieldList, definitionDataItem);
            if (!valid) {
                break;
            }
        }
        if (valid) {
            throw new MapperException(String.format("'retainHiddenValue' has been incorrectly configured or "
                    + "is invalid for fieldID ['%s'] on ['%s']",
                caseFieldId, SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
    }

    private void validateCaseEventToFields(DefinitionDataItem definitionDataItem,
                                           Map<String, DefinitionSheet> definitionSheets,
                                           List<DefinitionDataItem> caseField,
                                           List<DefinitionDataItem> caseEventToFieldsList) {
        DefinitionSheet complexTypes = definitionSheets.get(SheetName.COMPLEX_TYPES.getName());
        DefinitionSheet caseEventToFields = definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName());
        DefinitionSheet caseFields = definitionSheets.get(SheetName.CASE_FIELD.getName());

        boolean valid = true;
        String caseFieldId = null;
        for (DefinitionDataItem cf : caseField) {
            List<DefinitionDataItem> caseEventToFieldList = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> cf.getId()
                    .equals(definitionDataItem1.getCaseFieldId())).collect(toList());
            caseFieldId = cf.getId();
            valid = isSubFieldsIncorrectlyConfigured(definitionDataItem, caseEventToFieldList);
            if (valid) {
                break;
            }
        }

        if (Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())) {
            boolean invalidMatch = caseEventToFieldsList.stream()
                .noneMatch(definitionDataItem1 -> Boolean.TRUE.equals(definitionDataItem1.getRetainHiddenValue()));
            List<DefinitionDataItem> complexType = complexTypes.getDataItems().stream().filter(nestedComplexType ->
                nestedComplexType.getString(ColumnName.FIELD_TYPE)
                    .equals(definitionDataItem.getId())).collect(toList());

            for (DefinitionDataItem cf : complexType) {
                List<DefinitionDataItem> caseFieldList =
                    caseFields.getDataItems().stream().filter(caseFieldDataItem ->
                        cf.getId().equals(caseFieldDataItem
                            .getString(ColumnName.FIELD_TYPE))
                            || cf.getId().equals(caseFieldDataItem
                            .getString(ColumnName.FIELD_TYPE_PARAMETER))).collect(toList());

                for (DefinitionDataItem cfl : caseFieldList) {
                    List<DefinitionDataItem> caseEventToFieldList = caseEventToFields.getDataItems()
                        .stream().filter(definitionDataItem1 -> cfl.getId()
                            .equals(definitionDataItem1.getCaseFieldId())).collect(toList());
                    caseFieldId = cfl.getId();
                    valid = isSubFieldsIncorrectlyConfigured(definitionDataItem, caseEventToFieldList);
                    if (valid) {
                        break;
                    }
                }
            }
            if (!valid && invalidMatch) {
                throw new MapperException(String.format("'retainHiddenValue' has been incorrectly configured or "
                        + "is invalid for fieldID ['%s'] on ['%s']",
                    caseFieldId, SheetName.CASE_EVENT_TO_FIELDS.getName()));
            }
        }


        if (definitionDataItem.getRetainHiddenValue() != null && !valid) {
            throw new MapperException(String.format("'retainHiddenValue' can only be configured "
                    + "for a field that uses a "
                    + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                caseFieldId, SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
    }

    public Boolean parseCaseEventComplexTypesHiddenFields(DefinitionDataItem definitionDataItem,
                                                          Map<String, DefinitionSheet> definitionSheets) {
        if (definitionDataItem.getFieldShowCondition() != null
            && definitionDataItem.getRetainHiddenValue() != null) {
            verifyEventFieldHasRetainHiddenValue(definitionDataItem,
                definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }

        return definitionDataItem.getRetainHiddenValue();
    }


    private void verifyEventFieldHasRetainHiddenValue(DefinitionDataItem definitionDataItem,
                                                 DefinitionSheet caseEventToFieldsSheet) {
        List<DefinitionDataItem> caseEventToFields = caseEventToFieldsSheet.getDataItems().stream()
            .filter(caseEventToField -> caseEventToField.getCaseFieldId()
                .equals(definitionDataItem.getCaseFieldId()) && caseEventToField.getCaseEventId()
                .equals(definitionDataItem.getCaseEventId())).collect(toList());

        boolean showConditionConfigured = caseEventToFields.stream().noneMatch(ddi ->
            ddi.getFieldShowCondition() == null);

        if (!showConditionConfigured) {
            throw new MapperException(String.format("'retainHiddenValue' on CaseEventToComplexTypes can only be "
                    + "configured for a field that uses a showCondition. Field ['%s'] on ['%s'] "
                    + "does not use a showCondition",
                definitionDataItem.getCaseFieldId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
    }

    private Boolean isShowConditionPopulated(String fieldShowCondition, DefinitionDataItem definitionDataItem) {
        return (fieldShowCondition == null && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
    }

    private boolean isAtLeastOneCaseEventToFieldsConfigured(List<DefinitionDataItem> caseEventToFieldList,
                                                            DefinitionDataItem definitionDataItem) {
        boolean match;
        if (definitionDataItem.getRetainHiddenValue() != null && definitionDataItem.getFieldShowCondition() != null) {
            match = false;
        } else if (definitionDataItem.getRetainHiddenValue() != null
            && definitionDataItem.getFieldShowCondition() == null) {
            List<DefinitionDataItem> caseEventToFieldListFiltered =
                caseEventToFieldList.stream().filter(dataItem ->
                    dataItem.getString(ColumnName.FIELD_SHOW_CONDITION) != null).collect(toList());
            if (Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())) {
                match = (caseEventToFieldListFiltered.stream().anyMatch(dataItem ->
                    Boolean.TRUE.equals(dataItem.getRetainHiddenValue()))) ? false : true;
            } else {
                match = false;
            }
        } else {
            match = false;
        }
        return match;
    }

    private boolean isShowConditionNull(String fieldShowCondition, DefinitionDataItem definitionDataItem) {
        return (fieldShowCondition == null && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
    }

    private boolean isSubFieldsIncorrectlyConfigured(DefinitionDataItem definitionDataItem,
                                                     List<DefinitionDataItem> caseEventToFieldList) {
        return caseEventToFieldList.stream().anyMatch(definitionDataItem1 -> {
            Boolean caseFieldRetainHiddenValue = definitionDataItem1.getRetainHiddenValue();
            String caseFieldShowConditionValue = definitionDataItem1.getFieldShowCondition();
            if (definitionDataItem.getRetainHiddenValue() != null
                && definitionDataItem.getFieldShowCondition() != null) {
                return (caseFieldRetainHiddenValue == null
                    || Boolean.FALSE.equals(caseFieldRetainHiddenValue)) ? true : false;
            } else if (definitionDataItem.getRetainHiddenValue() != null
                && definitionDataItem.getFieldShowCondition() == null) {
                if (Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())
                    && (caseFieldRetainHiddenValue == null
                    || Boolean.FALSE.equals(caseFieldRetainHiddenValue))) {
                    return false;
                } else {
                    return caseFieldShowConditionValue == null ? false : true;
                }
            } else {
                return (Boolean.FALSE.equals(caseFieldRetainHiddenValue)
                    && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
            }
        });
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
