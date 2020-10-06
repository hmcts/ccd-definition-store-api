package uk.gov.hmcts.ccd.definition.store.domain.validation.regex;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.regex.Pattern;

public abstract class AbstractDocumentTypeRegularExpressionValidator {

    private static final Pattern DOCUMENT_REGULAR_EXPRESSION_PATTERN = Pattern.compile("\\.\\w+(,\\s*\\.\\w+)*");

    protected void validateDocumentType(FieldTypeEntity fieldType, ValidationResult validationResult) {
        if (fieldType.isDocumentType() && null != fieldType.getRegularExpression()
            && !DOCUMENT_REGULAR_EXPRESSION_PATTERN.matcher(fieldType.getRegularExpression()).matches()) {
            validationResult.addError(new RegularExpressionValidationError(fieldType));
        }
    }

}
