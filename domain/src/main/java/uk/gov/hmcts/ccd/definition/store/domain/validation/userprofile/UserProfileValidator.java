package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import java.util.List;

public interface UserProfileValidator {

    ValidationResult validate(List<WorkBasketUserDefault> workBasketUserDefaults,
                              JurisdictionEntity jurisdiction,
                              List<CaseTypeEntity> caseTypes);
}
