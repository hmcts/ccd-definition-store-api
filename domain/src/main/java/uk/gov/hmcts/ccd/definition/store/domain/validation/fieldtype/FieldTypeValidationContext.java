package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Collection;
import java.util.List;

public class FieldTypeValidationContext {
    private final Collection<FieldTypeEntity> baseTypes;
    private final Collection<FieldTypeEntity> baseComplexTypes;

    public FieldTypeValidationContext(List<FieldTypeEntity> baseTypes, List<FieldTypeEntity> baseComplexTypes) {
        this.baseTypes = baseTypes;
        this.baseComplexTypes = baseComplexTypes;
    }

    public Collection<FieldTypeEntity> getBaseTypes() {
        return baseTypes;
    }

    public Collection<FieldTypeEntity> getBaseComplexTypes() {
        return baseComplexTypes;
    }
}
