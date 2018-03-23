package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

public class UserProfileInvalidStateValidationError extends UserProfileValidatorImpl.ValidationError {

    public UserProfileInvalidStateValidationError(final WorkBasketUserDefault workBasketUserDefault) {
        super("state", workBasketUserDefault);
    }
}
