package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericLayoutEntityValidatorImpl implements GenericLayoutValidator {

    private static final String ERROR_MESSAGE_INVALID_CASE_TYPE_NOT_PRESENT =
        "Case Type cannot be empty for row with label '%s', case field '%s'";
    private static final String ERROR_MESSAGE_CASE_FIELD_NOT_PRESENT =
        "Case Field cannot be empty for row with label '%s', case type '%s'";
    private static final String ERROR_MESSAGE_INVALID_PATH =
        "Invalid ListElementCode '%s' for case type '%s', case field '%s' with label '%s'";
    private static final String ERROR_MESSAGE_PATH_DEFINED_FOR_NON_COMPLEX_FIELD =
        "ListElementCode '%s' can be only defined for complex fields. Case Field '%s', case type '%s'";
    static final String ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH =
        "Following have to be unique: [CaseTypeID '%s', CaseFieldID '%s', ListElementCode '%s'] with label '%s'";
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
    public ValidationResult validate(List<GenericLayoutEntity> genericLayoutEntities) {
        final ValidationResult validationResult = new ValidationResult();

        for (GenericLayoutEntity entity : genericLayoutEntities) {
            validateCaseTypeIsPresent(entity, validationResult);
            validateCaseFieldIsPresent(entity, validationResult);
            validatePaths(entity, validationResult);
            validateOrder(entity, validationResult);

            validateDuplicatesForTypeRefCaseRefAndPath(genericLayoutEntities, validationResult, entity);
        }

        return validationResult;
    }

    private void validateCaseTypeIsPresent(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseType() == null) {
            validationResult.addError(caseTypeNotPresentError(entity));
        }
    }

    private void validateCaseFieldIsPresent(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseField() == null) {
            validationResult.addError(caseFieldNotPresentError(entity));
        }
    }

    private void validatePaths(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (isNotBlank(entity.getCaseFieldElementPath())) {
            if (entity.getCaseField() != null
                && (entity.getCaseField().isComplexFieldType() || entity.getCaseField().isCollectionFieldType())) {
                List<CaseFieldEntity> caseFields = entity.getCaseType().getCaseFields();

                List<String> allPaths = caseFieldEntityUtil.buildDottedComplexFieldPossibilities(caseFields);
                if (!allPaths.contains(entity.getCaseField().getReference() + '.' + entity.getCaseFieldElementPath())) {
                    validationResult.addError(invalidPathError(entity));
                }
            } else {
                validationResult.addError(pathDefinedForNonComplexFieldError(entity));
            }
        }
    }

    private void validateOrder(final GenericLayoutEntity entity, final ValidationResult validationResult) {
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
    }

    private void validateDuplicatesForTypeRefCaseRefAndPath(final List<GenericLayoutEntity> genericLayouts,
                                                            final ValidationResult result,
                                                            final GenericLayoutEntity genericLayoutEntity) {
        genericLayouts.stream()
            .filter(e -> e != genericLayoutEntity)
            .filter(e -> e.getCaseField().getReference().equals(genericLayoutEntity.getCaseField().getReference()))
            .filter(e -> e.getCaseType().getReference().equals(genericLayoutEntity.getCaseType().getReference()))
            .filter(e -> StringUtils.equals(e.getCaseFieldElementPath(), genericLayoutEntity.getCaseFieldElementPath()))
            .findFirst().ifPresent(e -> result.addError(duplicateFoundError(e)));
    }

    private ValidationError caseFieldNotPresentError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_CASE_FIELD_NOT_PRESENT,
                entity.getLabel(),
                (entity.getCaseType() != null ? entity.getCaseType().getReference() : "")
            ), entity);
    }

    private ValidationError caseTypeNotPresentError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_INVALID_CASE_TYPE_NOT_PRESENT,
                entity.getLabel(),
                (entity.getCaseField() != null ? entity.getCaseField().getReference() : "")
            ), entity);
    }

    private ValidationError pathDefinedForNonComplexFieldError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_PATH_DEFINED_FOR_NON_COMPLEX_FIELD,
                entity.getCaseFieldElementPath(),
                entity.getCaseField().getReference(),
                entity.getCaseType().getReference()
            ), entity);
    }

    private ValidationError invalidPathError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_INVALID_PATH,
                entity.getCaseFieldElementPath(),
                entity.getCaseType().getReference(),
                entity.getCaseField().getReference(),
                entity.getLabel()
            ), entity);
    }

    private ValidationError duplicateFoundError(GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH,
                entity.getCaseType().getReference(),
                entity.getCaseField().getReference(),
                entity.getCaseFieldElementPath(),
                entity.getLabel()
            ), entity);
    }
}
