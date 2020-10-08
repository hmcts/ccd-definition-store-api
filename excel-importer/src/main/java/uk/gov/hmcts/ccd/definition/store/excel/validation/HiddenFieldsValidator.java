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
        Optional<DefinitionDataItem> caseField =
            caseFields.getDataItems().stream().filter(caseFieldDataItem ->
                definitionDataItem.getId().equals(caseFieldDataItem.getString(ColumnName.FIELD_TYPE))).findFirst();
        caseField.ifPresent(ddi -> {
            List<DefinitionDataItem> caseEventToFieldList = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> ddi.getId()
                .equals(definitionDataItem1.getCaseFieldId())).collect(toList());

            if (!isAtLeastOneCaseEventToFieldsConfigured(definitionDataItem, caseEventToFieldList)){
                throw new MapperException(String.format("'retainHiddenValue' can only be configured "
                        + "for a field that uses a "
                        + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                    ddi.getId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
            }

            Optional<DefinitionDataItem> caseEventToField = caseEventToFields.getDataItems()
                .stream().filter(definitionDataItem1 -> ddi.getId()
                    .equals(definitionDataItem1.getCaseFieldId())).findFirst();
            caseEventToField.ifPresent(caseEventToFieldDataItem -> {
                Boolean caseFieldRetainHiddenValue = caseEventToFieldDataItem.getRetainHiddenValue();
                if (Boolean.TRUE.equals(
                    isSubFieldsIncorrectlyConfigured(caseFieldRetainHiddenValue, definitionDataItem))) {
                    throw new MapperException(String.format(
                        "'retainHiddenValue' has been incorrectly configured or is invalid for "
                            + "fieldID ['%s'] on ['%s']",
                        caseEventToFieldDataItem.getCaseFieldId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
                }
                retainHiddenValue = definitionDataItem.getRetainHiddenValue();
            });
        });
        return retainHiddenValue;
    }

    private boolean isAtLeastOneCaseEventToFieldsConfigured(DefinitionDataItem definitionDataItem, List<DefinitionDataItem> caseEventToFieldList) {
        return caseEventToFieldList.stream().anyMatch(dataItem -> {
            String fieldShowCondition = dataItem.getString(ColumnName.FIELD_SHOW_CONDITION);
            return isShowConditionPopulated(fieldShowCondition, definitionDataItem);
        });
    }

    public Boolean parseCaseEventComplexTypesHiddenFields(DefinitionDataItem definitionDataItem,
                                                          Map<String, DefinitionSheet> definitionSheets) {
        final DefinitionSheet caseEventToFields = definitionSheets.get(SheetName.CASE_EVENT_TO_FIELDS.getName());
        List<DefinitionDataItem> caseEventToFieldList = caseEventToFields.getDataItems().stream()
            .filter(definitionDataItem1 -> definitionDataItem1.getCaseFieldId()
                .equals(definitionDataItem.getCaseFieldId()) && definitionDataItem1.getCaseEventId()
                .equals(definitionDataItem.getCaseEventId())).collect(toList());

        caseEventToFieldList.forEach(ddi -> {
            if (ddi.getFieldShowCondition() == null
                && definitionDataItem.getRetainHiddenValue() != null
                && definitionDataItem.getListElementCode().equals(ddi.getListElementCode())) {
                throw new MapperException(String.format("'retainHiddenValue' can only be configured "
                        + "for a field that uses a showCondition. Field ['%s'] on ['%s'] "
                        + "does not use a showCondition",
                    ddi.getCaseFieldId(), SheetName.CASE_EVENT_TO_FIELDS.getName()));
            }
        });
        if (definitionDataItem.getFieldShowCondition() == null) {
            throw new MapperException(String.format("'retainHiddenValue' can only be configured "
                    + "for a field that uses a showCondition. Field ['%s'] on ['%s'] "
                    + "does not use a showCondition",
                definitionDataItem.getCaseFieldId(), SheetName.CASE_EVENT_TO_COMPLEX_TYPES.getName()));
        }
        final DefinitionSheet complexTypes = definitionSheets.get(SheetName.COMPLEX_TYPES.getName());
        List<DefinitionDataItem> complexCaseFieldsList = complexTypes.getDataItems().stream()
            .filter(definitionDataItem1 -> definitionDataItem1.getId().equals(definitionDataItem.getId())
                && definitionDataItem1.getListElementCode().equals(definitionDataItem.getListElementCode()))
            .collect(toList());
        complexCaseFieldsList.forEach(definitionDataItem1 -> {
            if (definitionDataItem1.getFieldShowCondition() != null
                && Boolean.TRUE.equals(definitionDataItem1.getRetainHiddenValue())
                && Boolean.FALSE.equals(definitionDataItem.getRetainHiddenValue())
                && definitionDataItem.getFieldShowCondition() != null) {
                retainHiddenValue = definitionDataItem.getRetainHiddenValue();
            }
            if (definitionDataItem1.getFieldShowCondition() != null
                && Boolean.FALSE.equals(definitionDataItem1.getRetainHiddenValue())
                && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue())
                && definitionDataItem.getFieldShowCondition() != null) {
                retainHiddenValue = definitionDataItem.getRetainHiddenValue();
            } else {
                retainHiddenValue = definitionDataItem.getRetainHiddenValue();
            }
        });
        return retainHiddenValue;
    }

    private Boolean isShowConditionNull(String fieldShowCondition, DefinitionDataItem definitionDataItem) {
        return (fieldShowCondition == null && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
    }

    private Boolean isShowConditionPopulated(String fieldShowCondition, DefinitionDataItem definitionDataItem) {
        return (fieldShowCondition != null && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
    }

    private Boolean isSubFieldsIncorrectlyConfigured(Boolean caseFieldRetainHiddenValue,
                                                     DefinitionDataItem definitionDataItem) {
        return (Boolean.FALSE.equals(caseFieldRetainHiddenValue)
            && Boolean.TRUE.equals(definitionDataItem.getRetainHiddenValue()));
    }

    public Boolean parseHiddenFields(DefinitionDataItem definitionDataItem) {
        if (Boolean.TRUE.equals(isShowConditionNull(
            definitionDataItem.getString(ColumnName.FIELD_SHOW_CONDITION), definitionDataItem))) {
            throw new MapperException(String.format(
                "'retainHiddenValue' can only be configured for a field that uses a "
                    + "showCondition. Field ['%s'] on ['%s'] does not use a showCondition",
                definitionDataItem.getString(ColumnName.CASE_FIELD_ID), SheetName.CASE_EVENT_TO_FIELDS.getName()));
        }
        return definitionDataItem.getRetainHiddenValue();
    }
}
