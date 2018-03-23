package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

public interface GenericLayoutValidator {

    ValidationResult validate(GenericLayoutEntity genericLayoutEntity);

}
