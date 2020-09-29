package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class DisplayGroupColumnNumberValidator implements DisplayGroupCaseFieldValidator {

    private List<Predicate<Integer>> rules = newArrayList(
        Objects::isNull,
        colNr -> newArrayList(1, 2).contains(colNr)
    );

    @Override
    public ValidationResult validate(DisplayGroupCaseFieldEntity entity) {
        boolean anyMatch = rules.stream().anyMatch(p -> p.test(entity.getColumnNumber()));
        if (anyMatch) {
            return ValidationResult.SUCCESS;
        } else {
            String message = String.format("Invalid page column number '%s' for case field '%s'",
                entity.getColumnNumber(), entity.getCaseField().getReference());
            return new ValidationResult(new ValidationError(message, entity));
        }
    }

    public static class ValidationError extends SimpleValidationError<DisplayGroupCaseFieldEntity> {

        public ValidationError(String defaultMessage, DisplayGroupCaseFieldEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
