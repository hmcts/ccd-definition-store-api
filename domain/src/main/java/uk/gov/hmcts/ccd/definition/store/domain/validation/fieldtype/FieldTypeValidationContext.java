package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Collection;
import java.util.List;

public class FieldTypeValidationContext {
    private final Collection<FieldTypeEntity> baseTypes;

    public FieldTypeValidationContext(List<FieldTypeEntity> baseTypes) {
        this.baseTypes = baseTypes;
    }

    public Collection<FieldTypeEntity> getBaseTypes() {
        return baseTypes;
    }
}
