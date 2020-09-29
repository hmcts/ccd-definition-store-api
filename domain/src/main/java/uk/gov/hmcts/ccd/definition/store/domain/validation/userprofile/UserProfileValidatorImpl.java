package uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Referencable;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.lowerCase;

@Component
public class UserProfileValidatorImpl implements UserProfileValidator {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileValidatorImpl.class);

    private EmailValidator emailValidator = EmailValidator.getInstance();

    @Override
    public ValidationResult validate(final List<WorkBasketUserDefault> workBasketUserDefaults,
                                     final JurisdictionEntity jurisdiction,
                                     final List<CaseTypeEntity> caseTypes) {
        ValidationResult result = new ValidationResult();
        workBasketUserDefaults.forEach(w -> validate(result, w, jurisdiction, caseTypes));
        return result;
    }

    private void validate(final ValidationResult result,
                          final WorkBasketUserDefault workBasketUserDefault,
                          final JurisdictionEntity jurisdiction,
                          final List<CaseTypeEntity> caseTypes) {
        if (!emailValidator.isValid(lowerCase(workBasketUserDefault.getUserIdamId()))) {
            LOG.warn("Invalid email address found for {}", workBasketUserDefault);
            result.addError(new UserProfileInvalidEmailValidationError(workBasketUserDefault));
        }
        if (!StringUtils.equals(workBasketUserDefault.getWorkBasketDefaultJurisdiction(),
            jurisdiction.getReference())) {
            LOG.warn("Invalid jurisdiction found for {}", workBasketUserDefault);
            result.addError(new UserProfileInvalidJurisdictionValidationError(workBasketUserDefault));
        } else {
            final Optional<CaseTypeEntity> caseTypeFound = caseTypes.stream()
                .filter(referencePredicate(workBasketUserDefault.getWorkBasketDefaultCaseType()))
                .findFirst();
            if (!caseTypeFound.isPresent()) {
                LOG.warn("Invalid case type found for {}", workBasketUserDefault);
                result.addError(new UserProfileInvalidCaseTypeValidationError(workBasketUserDefault));
            } else {
                if (caseTypeFound.get()
                    .getStates()
                    .stream()
                    .noneMatch(referencePredicate(workBasketUserDefault.getWorkBasketDefaultState()))) {
                    LOG.warn("Invalid state found for {}", workBasketUserDefault);
                    result.addError(new UserProfileInvalidStateValidationError(workBasketUserDefault));
                }
            }
        }
    }

    private Predicate<Referencable> referencePredicate(final String reference) {
        return c -> StringUtils.equals(c.getReference(), reference);
    }

    public static class ValidationError extends SimpleValidationError<WorkBasketUserDefault> {

        public ValidationError(String invalidEntity, WorkBasketUserDefault workBasketUserDefault) {
            super(String.format("Invalid %s in workbasket user default; " //
                    + "user: '%s', jurisdiction: '%s', case type: '%s', state: '%s'",
                invalidEntity,
                workBasketUserDefault.getUserIdamId(),
                workBasketUserDefault.getWorkBasketDefaultJurisdiction(),
                workBasketUserDefault.getWorkBasketDefaultCaseType(),
                workBasketUserDefault.getWorkBasketDefaultState()), workBasketUserDefault);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
