package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class DisplayGroupCaseFieldElementPathValidatorImpl implements DisplayGroupCaseFieldValidator {

    private static final String ERROR_MESSAGE_INVALID_PATH =
        "Invalid ListElementCode '%s' for case type '%s', case field '%s'";
    private static final String ERROR_MESSAGE_PATH_DEFINED_FOR_NON_COMPLEX_FIELD =
        "ListElementCode '%s' can be only defined for complex fields. Case Field '%s', case type '%s'";
    private static final String ERROR_MESSAGE_PATH_DEFINED_FOR_COLLECTION_FIELD =
        "ListElementCode '%s' is not supported for collection fields. Case Field '%s', case type '%s'";

    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public DisplayGroupCaseFieldElementPathValidatorImpl(CaseFieldEntityUtil caseFieldEntityUtil) {
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(DisplayGroupCaseFieldEntity entity) {
        ValidationResult validationResult = new ValidationResult();
        CaseTypeEntity caseType = getCaseType(entity);
        if (entity.getCaseField() != null && caseType != null
            && isNotBlank(entity.getCaseFieldElementPath())) {
            validatePath(entity, caseType, validationResult);
        }
        return validationResult;
    }

    private void validatePath(DisplayGroupCaseFieldEntity entity,
                              CaseTypeEntity caseType,
                              ValidationResult validationResult) {
        if (entity.getCaseField().isCollectionFieldType()) {
            validationResult.addError(pathDefinedForCollectionFieldError(entity, caseType));
        } else if (entity.getCaseField().isComplexFieldType()) {
            validateComplexFieldPath(entity, caseType, validationResult);
        } else {
            validationResult.addError(pathDefinedForNonComplexFieldError(entity, caseType));
        }
    }

    private void validateComplexFieldPath(DisplayGroupCaseFieldEntity entity,
                                          CaseTypeEntity caseType,
                                          ValidationResult validationResult) {
        if (hasCollectionFieldInPath(entity)) {
            validationResult.addError(pathDefinedForCollectionFieldError(entity, caseType));
            return;
        }

        Set<CaseFieldEntity> caseFields = caseType.getCaseFields();
        Set<String> allPaths = caseFieldEntityUtil
            .buildDottedComplexFieldPossibilitiesIncludingParentComplexFields(caseFields);
        String fullPath = entity.getCaseField().getReference() + '.' + entity.getCaseFieldElementPath();

        if (!allPaths.contains(fullPath)) {
            validationResult.addError(invalidPathError(entity, caseType));
        }
    }

    private boolean hasCollectionFieldInPath(DisplayGroupCaseFieldEntity entity) {
        FieldEntity currentField = entity.getCaseField();
        for (String pathElement : entity.getCaseFieldElementPath().split("\\.")) {
            Optional<FieldEntity> nestedField = currentField.getFieldType().getChildren().stream()
                .filter(child -> child.getReference().equalsIgnoreCase(pathElement))
                .map(FieldEntity.class::cast)
                .findFirst();

            if (nestedField.isEmpty()) {
                return false;
            }

            if (nestedField.get().isCollectionFieldType()) {
                return true;
            }

            currentField = nestedField.get();
        }
        return false;
    }

    private CaseTypeEntity getCaseType(DisplayGroupCaseFieldEntity entity) {
        return entity.getDisplayGroup() == null ? null : entity.getDisplayGroup().getCaseType();
    }

    private ValidationError pathDefinedForNonComplexFieldError(DisplayGroupCaseFieldEntity entity,
                                                              CaseTypeEntity caseType) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_PATH_DEFINED_FOR_NON_COMPLEX_FIELD,
                entity.getCaseFieldElementPath(),
                entity.getCaseField().getReference(),
                caseType.getReference()), entity);
    }

    private ValidationError pathDefinedForCollectionFieldError(DisplayGroupCaseFieldEntity entity,
                                                              CaseTypeEntity caseType) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_PATH_DEFINED_FOR_COLLECTION_FIELD,
                entity.getCaseFieldElementPath(),
                entity.getCaseField().getReference(),
                caseType.getReference()), entity);
    }

    private ValidationError invalidPathError(DisplayGroupCaseFieldEntity entity,
                                            CaseTypeEntity caseType) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_INVALID_PATH,
                entity.getCaseFieldElementPath(),
                caseType.getReference(),
                entity.getCaseField().getReference()), entity);
    }

    public static class ValidationError extends SimpleValidationError<DisplayGroupCaseFieldEntity> {

        public ValidationError(String defaultMessage, DisplayGroupCaseFieldEntity entity) {
            super(defaultMessage, entity);
        }
    }
}
