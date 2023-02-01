package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class CannotOverridePredefinedComplexTypeValidationError extends ValidationError {

    private FieldTypeEntity fieldTypeEntity;

    private FieldTypeEntity conflictingFieldTypeEntity;

    public CannotOverridePredefinedComplexTypeValidationError(FieldTypeEntity fieldTypeEntity,
                                                              FieldTypeEntity conflictingFieldTypeEntity) {
        super(String.format(
            "Cannot override predefined complex type: %s", conflictingFieldTypeEntity.getReference()
        ));
        this.fieldTypeEntity = fieldTypeEntity;
        this.conflictingFieldTypeEntity = conflictingFieldTypeEntity;
    }

    public FieldTypeEntity getFieldTypeEntity() {
        return this.fieldTypeEntity;
    }

    public FieldTypeEntity getConflictingFieldTypeEntity() {
        return this.conflictingFieldTypeEntity;
    }

}
