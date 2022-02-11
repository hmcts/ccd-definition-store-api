package uk.gov.hmcts.ccd.definition.store.excel.parser;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.CategoryValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@Component
public class CategoryParser {

    private final CategoryValidator categoryValidator;

    @Autowired
    public CategoryParser(CategoryValidator categoryValidator) {
        this.categoryValidator = categoryValidator;
    }

    public List<CategoryEntity> parse(Map<String, DefinitionSheet> definitionSheets,
                                      ParseContext parseContext) {
        try {
            val categoriesItems = definitionSheets.get(SheetName.CATEGORY.getName()).getDataItems();
            categoryValidator.validate(parseContext, categoriesItems);
            final List<CategoryEntity> newCategoriesEntities = categoriesItems
                .stream().map(categoryItems ->
                    createCategoriesEntity(parseContext, categoryItems)
                ).collect(Collectors.toList());
            return newCategoriesEntities;
        } catch (InvalidImportException invalidImportException) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                @Override
                public String toString() {
                    return getDefaultMessage();
                }
            });
            throw new ValidationException(validationResult);
        }
    }

    public CategoryEntity createCategoriesEntity(ParseContext parseContext,
                                                 DefinitionDataItem definitionDataItem) {
        val categoriesEntity = new CategoryEntity();
        val  caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);

        val caseTypeEntityOptional = parseContext.getCaseTypes()
            .stream()
            .filter(caseTypeEntity -> caseTypeEntity.getReference().equals(caseType))
            .findAny();

        val caseTypeLiteEntity  = toCaseTypeLiteEntity(caseTypeEntityOptional.orElseThrow(InvalidImportException::new));
        categoriesEntity.setCaseType(caseTypeLiteEntity);

        categoriesEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
        categoriesEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
        categoriesEntity.setDisplayOrder(definitionDataItem.getInteger(ColumnName.DISPLAY_ORDER));
        categoriesEntity.setParentCategoryId(definitionDataItem.getString(ColumnName.PARENT_CATEGORY_ID));
        categoriesEntity.setCategoryLabel(definitionDataItem.getString(ColumnName.CATEGORY_LABEL));
        categoriesEntity.setCategoryId(definitionDataItem.getString(ColumnName.CATEGORY_ID));
        return categoriesEntity;
    }
}
