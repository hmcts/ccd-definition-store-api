package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
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

    private void validate(ParseContext parseContext,
                          SheetName sheetName,
                          String caseTypeId,
                          String categoryId,
                          FieldTypeEntity fieldType) {
        if (categoryId != null) {
            if (fieldType.getReference().equals(FieldTypeUtils.BASE_DOCUMENT)
                || (fieldType.getBaseFieldType() != null
                && fieldType.getCollectionFieldType() != null
                && fieldType.getCollectionFieldType().getReference().equals(FieldTypeUtils.BASE_DOCUMENT)
                && fieldType.getBaseFieldType().getReference().equals(FieldTypeUtils.BASE_COLLECTION))
            ) {
                if (parseContext.getCategory(caseTypeId, categoryId) == null) {
                    throw new InvalidImportException(
                        String.format(ERROR_MESSAGE_INVALID_CATEGORY, sheetName, categoryId));
                }
            } else {
                throw new InvalidImportException(
                    String.format(ERROR_MESSAGE_INVALID_FIELD_TYPE, sheetName, categoryId));
            }
        }
    }

    public void validate(ParseContext parseContext) {
        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            for (CaseFieldEntity caseField : caseType.getCaseFields()) {
                validate(parseContext, SheetName.CASE_FIELD, caseType.getReference(),
                    caseField.getCategory(), caseField.getFieldType());
            }
        }
    }
}
