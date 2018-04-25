package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;

import java.util.Collection;

public interface DisplayGroupValidator {

    ValidationResult validate(DisplayGroupEntity displayGroup, Collection<DisplayGroupEntity> allDisplayGroups);

}
