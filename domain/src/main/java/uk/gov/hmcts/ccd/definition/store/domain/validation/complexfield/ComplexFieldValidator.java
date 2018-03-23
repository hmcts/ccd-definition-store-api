package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public interface ComplexFieldValidator {

    ValidationResult validate(ComplexFieldEntity complexField, ValidationContext validationContext);


    class ValidationContext {

        private FieldTypeEntity complexFieldType;

        public ValidationContext(FieldTypeEntity complexFieldType) {
            this.complexFieldType = complexFieldType;
        }

        public FieldTypeEntity getComplexFieldType() {
            return complexFieldType;
        }
    }
}
