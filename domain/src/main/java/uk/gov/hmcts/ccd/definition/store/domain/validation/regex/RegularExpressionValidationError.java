package uk.gov.hmcts.ccd.definition.store.domain.validation.regex;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class RegularExpressionValidationError extends ValidationError {

    private static final long serialVersionUID = 2119816255851898972L;

    public RegularExpressionValidationError(FieldTypeEntity fieldTypeEntity) {
        super(String.format("Invalid regular expression: %s", fieldTypeEntity.getRegularExpression()));
    }

}
