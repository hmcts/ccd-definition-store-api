package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

public class ComplexFieldEntityIncorrectOrderValidationError extends ValidationError {

    private FieldEntity fieldEntity;
    private Integer index;
    private ComplexFieldEntity nestedFieldEntity;

    public ComplexFieldEntityIncorrectOrderValidationError(FieldEntity complexFieldEntity, int index, ComplexFieldEntity nestedFieldEntity) {
        super(String.format("ComplexField with reference=%s has incorrect order at index=%s for nested fieldReference=%s",
                            complexFieldEntity.getReference(), index, nestedFieldEntity.getReference()));
        this.fieldEntity = complexFieldEntity;
        this.index = index;
        this.nestedFieldEntity = nestedFieldEntity;
    }

    public FieldEntity getFieldEntity() {
        return fieldEntity;
    }

    public Integer getIndex() {
        return index;
    }

    public ComplexFieldEntity getNestedFieldEntity() {
        return nestedFieldEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

}
