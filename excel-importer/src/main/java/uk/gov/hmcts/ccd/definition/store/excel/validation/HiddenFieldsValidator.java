package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

@Component
public class HiddenFieldsValidator {
    public Boolean parseComplexTypesHiddenFields(DefinitionDataItem definitionDataItem,
                                                 Map<String, DefinitionSheet> definitionSheets) {
        return parseComplexTypesHiddenFields(definitionDataItem, new ComplexTypesValidationIndex(definitionSheets));
    }

    private Boolean parseComplexTypesHiddenFields(DefinitionDataItem definitionDataItem,
                                                  ComplexTypesValidationIndex index) {
        final String definitionItemId = definitionDataItem.getId();

        List<DefinitionDataItem> caseFieldList = index.caseFieldsByType.getOrDefault(
            definitionItemId, List.of());
        List<DefinitionDataItem> caseEventToFieldListFiltered = new ArrayList<>();
        for (DefinitionDataItem cf : caseFieldList) {
            caseEventToFieldListFiltered.addAll(index.caseEventToFieldsByCaseField.getOrDefault(
                cf.getId(), List.of()));
        }

        validateCaseEventToFields(definitionDataItem, index, caseFieldList, caseEventToFieldListFiltered);
        validateSubFieldConfiguration(caseFieldList, definitionDataItem, index);

        return definitionDataItem.getRetainHiddenValue();
    }

    public void validateComplexTypesHiddenFields(Collection<List<DefinitionDataItem>> complexTypes,
                                                 Map<String, DefinitionSheet> definitionSheets) {
        ComplexTypesValidationIndex index = new ComplexTypesValidationIndex(definitionSheets);
        List<String> errors = new ArrayList<>();
        for (List<DefinitionDataItem> complexType : complexTypes) {
            for (DefinitionDataItem definitionDataItem : complexType) {
                try {
                    parseComplexTypesHiddenFields(definitionDataItem, index);
                } catch (RuntimeException exception) {
                    errors.add(exception.getMessage());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new MapperException(String.join("\n", errors));
        }
    }

    private void validateSubFieldConfiguration(List<DefinitionDataItem> caseField,
                                               DefinitionDataItem definitionDataItem,
                                               ComplexTypesValidationIndex index) {

        boolean valid = false;
        String caseFieldId = null;
        for (DefinitionDataItem cf : caseField) {
            List<DefinitionDataItem> caseEventToFieldList =
                index.caseEventToFieldsByCaseField.getOrDefault(cf.getId(), List.of());
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
                                           ComplexTypesValidationIndex index,
                                           List<DefinitionDataItem> caseField,
                                           List<DefinitionDataItem> caseEventToFieldsList) {
        boolean valid = true;
        String caseFieldId = null;
        for (DefinitionDataItem cf : caseField) {
            List<DefinitionDataItem> caseEventToFieldList =
                index.caseEventToFieldsByCaseField.getOrDefault(cf.getId(), List.of());
            caseFieldId = cf.getId();
            valid = isSubFieldsIncorrectlyConfigured(definitionDataItem, caseEventToFieldList);
            if (valid) {
                break;
            }
        }

        if (Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())) {
            boolean invalidMatch = caseEventToFieldsList.stream()
                .noneMatch(definitionDataItem1 -> Boolean.TRUE.equals(definitionDataItem1.getRetainHiddenValue()));
            List<DefinitionDataItem> complexType = index.complexTypesByFieldType().getOrDefault(
                definitionDataItem.getId(), List.of());

            for (DefinitionDataItem cf : complexType) {
                List<DefinitionDataItem> caseFieldList =
                    index.caseFieldsByType.getOrDefault(cf.getId(), List.of());

                for (DefinitionDataItem cfl : caseFieldList) {
                    List<DefinitionDataItem> caseEventToFieldList =
                        index.caseEventToFieldsByCaseField.getOrDefault(cfl.getId(), List.of());
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
                match = caseEventToFieldListFiltered.stream().noneMatch(dataItem ->
                    Boolean.TRUE.equals(dataItem.getRetainHiddenValue()));
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
                return caseFieldRetainHiddenValue == null
                    || Boolean.FALSE.equals(caseFieldRetainHiddenValue);
            } else if (definitionDataItem.getRetainHiddenValue() != null
                && definitionDataItem.getFieldShowCondition() == null) {
                if (Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())
                    && (caseFieldRetainHiddenValue == null
                    || Boolean.FALSE.equals(caseFieldRetainHiddenValue))) {
                    return false;
                } else {
                    return caseFieldShowConditionValue != null;
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

    private static class ComplexTypesValidationIndex {
        private final Map<String, List<DefinitionDataItem>> caseFieldsByType = new HashMap<>();
        private final Map<String, List<DefinitionDataItem>> caseEventToFieldsByCaseField = new HashMap<>();
        private final DefinitionSheet complexTypes;
        private Map<String, List<DefinitionDataItem>> complexTypesByFieldType;

        private ComplexTypesValidationIndex(Map<String, DefinitionSheet> definitionSheets) {
            complexTypes = definitionSheets.get(SheetName.COMPLEX_TYPES.getName());
            for (DefinitionDataItem caseField : definitionSheets.get(
                SheetName.CASE_FIELD.getName()).getDataItems()) {
                String fieldType = caseField.getString(ColumnName.FIELD_TYPE);
                String fieldTypeParameter = caseField.getString(ColumnName.FIELD_TYPE_PARAMETER);
                add(caseFieldsByType, fieldType, caseField);
                if (!Objects.equals(fieldType, fieldTypeParameter)) {
                    add(caseFieldsByType, fieldTypeParameter, caseField);
                }
            }
            for (DefinitionDataItem caseEventToField : definitionSheets.get(
                SheetName.CASE_EVENT_TO_FIELDS.getName()).getDataItems()) {
                add(caseEventToFieldsByCaseField, caseEventToField.getCaseFieldId(), caseEventToField);
            }
        }

        private Map<String, List<DefinitionDataItem>> complexTypesByFieldType() {
            if (complexTypesByFieldType == null) {
                complexTypesByFieldType = new HashMap<>();
                for (DefinitionDataItem complexType : complexTypes.getDataItems()) {
                    add(complexTypesByFieldType, complexType.getString(ColumnName.FIELD_TYPE), complexType);
                }
            }
            return complexTypesByFieldType;
        }

        private static void add(Map<String, List<DefinitionDataItem>> index,
                                String key,
                                DefinitionDataItem dataItem) {
            if (key != null) {
                index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(dataItem);
            }
        }
    }
}
