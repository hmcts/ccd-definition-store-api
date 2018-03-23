package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public interface CaseFieldEntityValidator {

    ValidationResult validate(CaseFieldEntity caseField,
                              CaseFieldEntityValidationContext caseFieldEntityValidationContext);

}
