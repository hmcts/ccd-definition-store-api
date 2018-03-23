package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

public class UserProfileInvalidEmailValidationError extends UserProfileValidatorImpl.ValidationError {

    public UserProfileInvalidEmailValidationError(final WorkBasketUserDefault workBasketUserDefault) {
        super("email", workBasketUserDefault);
    }
}
