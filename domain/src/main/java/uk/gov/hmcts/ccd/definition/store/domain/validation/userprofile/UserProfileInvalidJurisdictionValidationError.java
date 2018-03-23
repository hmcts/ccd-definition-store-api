package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

public class UserProfileInvalidJurisdictionValidationError extends UserProfileValidatorImpl.ValidationError {

    public UserProfileInvalidJurisdictionValidationError(final WorkBasketUserDefault workBasketUserDefault) {
        super("jurisdiction", workBasketUserDefault);
    }
}

