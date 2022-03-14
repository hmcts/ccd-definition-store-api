package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

@Component
public class CategoryIdValidator {
    protected static final String ERROR_MESSAGE =
        "%sTab Invalid value '%s' is not a valid " + ColumnName.CATEGORY_ID + " value. ";
    protected static final String ERROR_MESSAGE_INVALID_CATEGORY =
        ERROR_MESSAGE + "Category cannot be found.";
    protected static final String ERROR_MESSAGE_INVALID_FIELD_TYPE =
        ERROR_MESSAGE + "Category not permitted for this field type.";


    public CategoryIdValidator() {
    }

    public void validate(ParseContext parseContext) {
        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            for (CaseFieldEntity caseField : caseType.getCaseFields()) {
                validateCaseField(parseContext, caseType.getReference(),
                    caseField.getCategoryId(), caseField.getFieldType());
            }
        }
        for (FieldTypeEntity complexType : parseContext.getComplexTypes()) {
            for(ComplexFieldEntity complexField : complexType.getComplexFields()) {
                validateComplexField(parseContext, complexField);
            }
        }
    }

    private void validateCaseField(ParseContext parseContext,
                          String caseTypeId,
                          String categoryId,
                          FieldTypeEntity fieldType) {

        //If category is null then that is valid value
        if (!StringUtils.isEmpty(categoryId)) {
            //invalid if category is Document or Collection of Document
            if (!validFieldType(fieldType)) {
                throw new InvalidImportException(
                    String.format(ERROR_MESSAGE_INVALID_FIELD_TYPE, SheetName.CASE_FIELD, categoryId));
            }

            //Invalid if categoryId does not match an Id in the categories tab
            if (parseContext.getCategory(caseTypeId, categoryId) == null) {
                throw new InvalidImportException(
                    String.format(ERROR_MESSAGE_INVALID_CATEGORY, SheetName.CASE_FIELD, categoryId));
            }
        }
    }

    private void validateComplexField(ParseContext parseContext,
                                   ComplexFieldEntity complexField) {

        String categoryId = complexField.getCategoryId();

        //If category is null then that is valid value
        if (!StringUtils.isEmpty(categoryId)) {
            //invalid if category is Document or Collection of Document
            if (!validFieldType(complexField.getFieldType())) {
                throw new InvalidImportException(
                    String.format(ERROR_MESSAGE_INVALID_FIELD_TYPE, SheetName.COMPLEX_TYPES, categoryId));
            }

            //Invalid if categoryId does not match an Id in the categories tab
            if (!parseContext.checkCategoryExists(categoryId)) {
                throw new InvalidImportException(
                    String.format(ERROR_MESSAGE_INVALID_CATEGORY, SheetName.COMPLEX_TYPES, categoryId));
            }
        }
    }

    public boolean validFieldType(FieldTypeEntity fieldType) {
        return fieldType.getReference().equals(FieldTypeUtils.BASE_DOCUMENT)
            || (fieldType.getBaseFieldType() != null
            && fieldType.getCollectionFieldType() != null
            && fieldType.getBaseFieldType().getReference().equals(FieldTypeUtils.BASE_COLLECTION)
            && fieldType.getCollectionFieldType().getReference().equals(FieldTypeUtils.BASE_DOCUMENT));
    }

}
