package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

public interface GenericLayoutValidator {

    ValidationResult validate(GenericLayoutEntity genericLayoutEntity, List<GenericLayoutEntity> allGenericLayouts);

    class ValidationError extends SimpleValidationError<GenericLayoutEntity> {

        private static final long serialVersionUID = 3568584589714415110L;

        public ValidationError(String defaultMessage, GenericLayoutEntity entity) {
            super(defaultMessage, entity);
        }
    }
}
