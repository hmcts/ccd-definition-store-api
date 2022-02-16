package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Pair;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Triple;
import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class CategoryValidator {

    private static final String ERROR_MESSAGE = "Categories tab Invalid ";
    private static final String NOT_VALID = " is not a valid ";
    private static final String DISPLAY_ORDER_ERROR = "DisplayOrder cannot be duplicated within case type"
        + " category and parent category in Category tab";


    public CategoryValidator() {
    }

    public void validate(ParseContext parseContext, List<DefinitionDataItem> categoryItems) {
        validateUniqueCategoriesIds(categoryItems);
        validateUniqueDisplayOrder(categoryItems);
        val pairCaseTypeCategory = createCategoryIdCaseTypeMap(categoryItems);
        categoryItems.forEach(element -> validate(element, pairCaseTypeCategory,parseContext));
    }

    private void validate(DefinitionDataItem definitionDataItem,Map<String, List<String>> pairCaseTypeCategory,
                          ParseContext parseContext

    ) {
        validateCaseType(definitionDataItem,parseContext);
        validateCategoryId(definitionDataItem);
        validateCategoryLabel(definitionDataItem);
        validateDisplayOrder(definitionDataItem);
        validateParentCategoryID(definitionDataItem,pairCaseTypeCategory);
    }

    private void validateParentCategoryID(DefinitionDataItem definitionDataItem,
                                          Map<String, List<String>> pairCaseTypeCategory) {

        val parentCategoryId = definitionDataItem.getString(ColumnName.PARENT_CATEGORY_ID);
        if (!StringUtils.isEmpty(parentCategoryId)) {
            val error = new InvalidImportException(
                ERROR_MESSAGE + "ParentCategoryID: " + parentCategoryId + " belongs to a different case type."
            );
            val currentCaseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
            val listOfCategoriesIdsForACaseType = pairCaseTypeCategory.get(currentCaseType);

            if (listOfCategoriesIdsForACaseType.isEmpty()
                | !listOfCategoriesIdsForACaseType.contains(parentCategoryId)) {

                throw error;
            }
        }
    }

    private void validateUniqueCategoriesIds(List<DefinitionDataItem> categoryItems) {
        final Map<Pair<String, String>, List<DefinitionDataItem>> caseTypeCategoryItems =
            categoryItems
                .stream()
                .collect(groupingBy(p ->
                    Pair.of(
                        p.getString(ColumnName.CASE_TYPE_ID),
                        p.getString(ColumnName.CATEGORY_ID))
                ));

        caseTypeCategoryItems.keySet()
            .forEach(pair -> {
                if (caseTypeCategoryItems.get(pair).size() > 1) {
                    throw new InvalidImportException(
                        ColumnName.CATEGORY_ID + " value:" + pair + " cannot be duplicated within case type.");
                }
            });
    }

    private Map<String, List<String>> createCategoryIdCaseTypeMap(List<DefinitionDataItem> categoryItems) {

        return categoryItems.stream().map(pair -> {
                val caseTypeId = pair.getString(ColumnName.CASE_TYPE_ID);
                validateNullValue(caseTypeId, ERROR_MESSAGE + "value: " + caseTypeId + " CaseTypeId cannot be null.");
                return Pair.of(pair.getString(ColumnName.CASE_TYPE_ID), pair.getString(ColumnName.CATEGORY_ID));
            }
        ).collect(
            Collectors.groupingBy(Pair::getKey,Collectors.mapping(Pair::getValue, Collectors.toList())
        ));
    }

    private void validateUniqueDisplayOrder(List<DefinitionDataItem> categories) {

        validateUniqueDisplayOrderForItems(
            categories, Arrays.asList(ColumnName.CASE_TYPE_ID, ColumnName.CATEGORY_ID, ColumnName.DISPLAY_ORDER)
        );

        validateUniqueDisplayOrderForItems(
            categories, Arrays.asList(ColumnName.CASE_TYPE_ID, ColumnName.PARENT_CATEGORY_ID, ColumnName.DISPLAY_ORDER)
        );
    }

    private void validateUniqueDisplayOrderForItems(List<DefinitionDataItem> categoryItems, List<ColumnName> fields) {
        Map<Triple<String, String, String>, List<DefinitionDataItem>> categoriesDisplayOrder =
            categoryItems
                .stream()
                .collect(groupingBy(p ->
                    Triple.of(
                        p.getString(fields.get(0)),
                        p.getString(fields.get(1)),
                        p.getString(fields.get(2))
                    )));

        categoriesDisplayOrder.keySet()
            .forEach(triple -> {
                if (categoriesDisplayOrder.get(triple).size() > 1) {
                    throw new InvalidImportException(DISPLAY_ORDER_ERROR);
                }
            });
    }

    private void validateCategoryId(DefinitionDataItem definitionDataItem) {
        final String id = definitionDataItem.getString(ColumnName.CATEGORY_ID);
        validateNullValue(id, ERROR_MESSAGE + "value: " + id + " category id cannot be null.");
    }

    private void validateCategoryLabel(DefinitionDataItem definitionDataItem) {
        final String id = definitionDataItem.getString(ColumnName.CATEGORY_LABEL);
        validateNullValue(id, ERROR_MESSAGE + "value: " + id + " category label cannot be null.");
    }

    private void validateDisplayOrder(DefinitionDataItem definitionDataItem) {
        final String displayOrder = definitionDataItem.getString(ColumnName.DISPLAY_ORDER);
        try {
            if (Integer.parseInt(displayOrder) < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException numberFormatException) {
            throw new InvalidImportException(
                ERROR_MESSAGE + "value: " + displayOrder + NOT_VALID + "DisplayOrder.");
        }
    }

    private CaseTypeEntity validateCaseType(DefinitionDataItem categoryItem, ParseContext parseContext) {
        final String caseType = categoryItem.getString(ColumnName.CASE_TYPE_ID);
        Optional<CaseTypeEntity> caseTypeEntityOptional = parseContext.getCaseTypes()
            .stream()
            .filter(caseTypeEntity -> caseTypeEntity.getReference().equals(caseType))
            .findAny();
        return caseTypeEntityOptional.orElseThrow(() -> new InvalidImportException(
            ERROR_MESSAGE + " Case Type value: " + caseType + ". It cannot be found in the spreadsheet.")
        );
    }


    private void validateNullValue(String value, String message) {
        if (value == null) {
            throw new InvalidImportException(message);
        }
    }
}
