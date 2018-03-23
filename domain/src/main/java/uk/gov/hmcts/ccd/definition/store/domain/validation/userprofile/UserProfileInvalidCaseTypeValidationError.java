package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

public class UserProfileInvalidCaseTypeValidationError extends UserProfileValidatorImpl.ValidationError {

    public UserProfileInvalidCaseTypeValidationError(final WorkBasketUserDefault workBasketUserDefault) {
        super("case type", workBasketUserDefault);
    }
}
