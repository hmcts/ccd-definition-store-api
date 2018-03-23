package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public interface FieldTypeValidator {
    ValidationResult validate(FieldTypeValidationContext context, FieldTypeEntity fieldType);
}
