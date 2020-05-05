package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.List;

public interface CaseFieldComplexFieldEntityValidator {

    ValidationResult validate(ComplexFieldEntity complexField,
                              ValidationContext validationContext);

    class ValidationContext {

        private SecurityClassification parentSecurityClassification;
        private String caseName;
        private String caseFieldReference;
        private List<FieldTypeEntity> preDefinedComplexTypes;

        public ValidationContext(CaseFieldEntityValidationContext caseFieldEntityValidationContext,
                                 CaseFieldEntity caseField,
                                 List<FieldTypeEntity> preDefinedComplexTypes) {
            this.caseName = caseFieldEntityValidationContext.getCaseName();
            this.caseFieldReference = caseField.getReference();
            this.parentSecurityClassification = caseField.getSecurityClassification();
            this.preDefinedComplexTypes = preDefinedComplexTypes;
        }

        public SecurityClassification getParentSecurityClassification() {
            return this.parentSecurityClassification;
        }

        public String getCaseName() {
            return this.caseName;
        }

        public String getCaseFieldReference() {
            return this.caseFieldReference;
        }

        public List<FieldTypeEntity> getPreDefinedComplexTypes() {
            return this.preDefinedComplexTypes;
        }

        class ValidationError extends SimpleValidationError<EventComplexTypeEntity> {

            private static final long serialVersionUID = -3141095128631384821L;

            public ValidationError(String defaultMessage, EventComplexTypeEntity entity) {
                super(defaultMessage, entity);
            }
        }

    }

}
