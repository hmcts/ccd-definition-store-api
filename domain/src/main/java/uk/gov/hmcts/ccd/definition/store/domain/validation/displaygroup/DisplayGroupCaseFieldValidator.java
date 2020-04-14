package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;

public interface DisplayGroupCaseFieldValidator {

    ValidationResult validate(DisplayGroupCaseFieldEntity entity);
}
