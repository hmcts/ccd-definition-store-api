package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public class CannotOverrideBaseTypeValidationError extends ValidationError {

    private FieldTypeEntity fieldTypeEntity;

    private FieldTypeEntity conflictingFieldTypeEntity;

    public CannotOverrideBaseTypeValidationError(FieldTypeEntity fieldTypeEntity,
                                                 FieldTypeEntity conflictingFieldTypeEntity) {
        super(String.format(
            "Cannot override base type: %s", conflictingFieldTypeEntity.getReference()
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
