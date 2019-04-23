package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericLayoutEntityValidatorImpl implements GenericLayoutValidator {

    private static final String ERROR_MESSAGE_INVALID_NUMBER_WITH_CASE_FIELD =
        "DisplayOrder '%d' needs to be a valid integer for row with label '%s', case field '%s'";
    private static final String ERROR_MESSAGE_INVALID_NUMBER_WITHOUT_CASE_FIELD =
        "DisplayOrder '%d' needs to be a valid integer for row with label '%s'";

    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public GenericLayoutEntityValidatorImpl(CaseFieldEntityUtil caseFieldEntityUtil) {
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(GenericLayoutEntity entity) {
        final ValidationResult validationResult = new ValidationResult();
        if (entity.getCaseType() == null) {
            validationResult.addError(
                new ValidationError(
                    String.format("Case Type cannot be empty for row with label '%s', case field '%s'",
                        entity.getLabel(),
                        (entity.getCaseField() != null ? entity.getCaseField().getReference() : "")
                    ), entity)
            );
        }
        if (entity.getCaseField() == null) {
            validationResult.addError(
                new ValidationError(
                    String.format("Case Field cannot be empty for row with label '%s', case type '%s'",
                        entity.getLabel(),
                        (entity.getCaseType() != null ? entity.getCaseType().getReference() : "")
                    ), entity)
            );
        } else {
            validatePaths(entity, validationResult);
        }

        if (entity.getOrder() != null && entity.getOrder() < 1) {
            final String errorMessage;
            if (null == entity.getCaseField()) {
                errorMessage =
                    String.format(ERROR_MESSAGE_INVALID_NUMBER_WITHOUT_CASE_FIELD,
                                  entity.getOrder(),
                                  entity.getLabel());
            } else {
                errorMessage =
                    String.format(ERROR_MESSAGE_INVALID_NUMBER_WITH_CASE_FIELD,
                                  entity.getOrder(),
                                  entity.getLabel(),
                                  entity.getCaseField().getReference());
            }
            validationResult.addError(new ValidationError(errorMessage, entity));
        }
        return validationResult;
    }

    private void validatePaths(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseFieldElementPath() != null) {
            if (entity.getCaseField().isComplexFieldType() || entity.getCaseField().isCollectionFieldType()) {
                List<CaseFieldEntity> caseFields = entity.getCaseType().getCaseFields();

                List<String> allPaths = caseFieldEntityUtil.buildDottedComplexFieldPossibilities(caseFields);
                if (!allPaths.contains(entity.getCaseField().getReference() + '.' + entity.getCaseFieldElementPath())) {
                    validationResult.addError(
                        new ValidationError(
                            String.format("Invalid ListElementCode '%s' for case type '%s', case field '%s' with label '%s'",
                                entity.getCaseFieldElementPath(),
                                entity.getCaseType().getReference(),
                                entity.getCaseField().getReference(),
                                entity.getLabel()
                            ), entity)
                    );
                }
            } else {
                validationResult.addError(
                    new ValidationError(
                        String.format("ListElementCode '%s' can be only defined for complex fields. Case Field '%s', case type '%s'",
                            entity.getCaseFieldElementPath(),
                            entity.getCaseField().getReference(),
                            entity.getCaseType().getReference()
                        ), entity)
                );
            }
        }
    }

    public static class ValidationError extends SimpleValidationError<GenericLayoutEntity> {

        public ValidationError(String defaultMessage, GenericLayoutEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
