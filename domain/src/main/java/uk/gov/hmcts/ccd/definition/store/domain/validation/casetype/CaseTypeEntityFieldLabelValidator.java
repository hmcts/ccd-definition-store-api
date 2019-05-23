package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import java.util.Optional;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ArrayUtils.subarray;

@Component
public class CaseTypeEntityFieldLabelValidator implements CaseTypeEntityValidator {

    private static final String PLACEHOLDER_PATTERN = "^[a-zA-Z0-9_.\\]\\[]+$";
    private static final char STARTING_PLACEHOLDER = '$';
    private static final char OPENING_PLACEHOLDER = '{';
    private static final char CLOSING_PLACEHOLDER = '}';

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {

        ValidationResult validationResult = new ValidationResult();
        // go over each field label and check for placeholders
        caseType.getCaseFields().stream().forEach(caseFieldEntity -> {
            String label = ofNullable(caseFieldEntity.getLabel()).orElse("");
            boolean isCollecting = false;
            String placeholderToSubstitute = "";
            for (int scanIndex = 0; scanIndex < label.length(); scanIndex++) {
                char c = label.charAt(scanIndex);
                if (isStartPlaceholderAndNotCollecting(label, scanIndex, isCollecting)) {
                    isCollecting = true;
                } else if (isCollecting) {
                    if (isClosingPlaceholder(label, scanIndex)) {
                        if (isMatchingPlaceholderPattern(placeholderToSubstitute)) {
                            boolean hasFoundPlaceholderField = false;
                            for (CaseFieldEntity lookupEntity : caseType.getCaseFields()) {
                                String[] fieldIds = placeholderToSubstitute.split("\\.");
                                if (lookupEntity.getReference().equals(fieldIds[0])) {
                                    Optional<FieldEntity> nestedElementByPath = lookupEntity.findNestedElementByPath(join(".", subarray(fieldIds,
                                                                                                                                        1,
                                                                                                                                        fieldIds.length)));
                                    if (nestedElementByPath.isPresent()) {
                                        hasFoundPlaceholderField = true;
                                        FieldEntity nestedElement = nestedElementByPath.get();
                                        if (nestedElement.isComplexFieldType() || nestedElement.isCollectionFieldType()) {
                                            validationResult.addError(new PlaceholderLeafNotSimpleTypeValidationError(caseFieldEntity.getReference(),
                                                                                                                      placeholderToSubstitute,
                                                                                                                      nestedElement.getReference(),
                                                                                                                      caseFieldEntity));
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!hasFoundPlaceholderField) {
                                validationResult.addError(new PlaceholderCannotBeResolvedValidationError(caseFieldEntity.getReference(),
                                                                                                         placeholderToSubstitute,
                                                                                                         caseFieldEntity));
                            }
                        }
                        isCollecting = false;
                        placeholderToSubstitute = "";
                    } else if (!isOpeningPlaceholder(label, scanIndex)) {
                        placeholderToSubstitute += label.charAt(scanIndex);
                    }
                }
            }
        });
        return validationResult;
    }

    private boolean isMatchingPlaceholderPattern(String fieldIdToSubstitute) {
        return fieldIdToSubstitute.matches(PLACEHOLDER_PATTERN);
    }

    private boolean isClosingPlaceholder(String label, int scanIndex) {
        return label.charAt(scanIndex) == CLOSING_PLACEHOLDER;
    }

    private boolean isOpeningPlaceholder(String label, int scanIndex) {
        return label.charAt(scanIndex) == OPENING_PLACEHOLDER;
    }

    private boolean isStartPlaceholderAndNotCollecting(String label, int scanIndex, boolean isCollectingPlaceholder) {
        return isStartingPlaceholder(label, scanIndex) && !isCollectingPlaceholder;
    }

    private boolean isStartingPlaceholder(String label, int scanIndex) {
        return label.charAt(scanIndex) == STARTING_PLACEHOLDER;
    }

    public static class PlaceholderLeafNotSimpleTypeValidationError extends SimpleValidationError<CaseFieldEntity> {
        public PlaceholderLeafNotSimpleTypeValidationError(String caseFieldReference, String placeholder, String leafReference, CaseFieldEntity entity) {
            super(format("Label of caseField '%s' has placeholder '%s' that points to case field '%s' of non simple type",
                         caseFieldReference,
                         placeholder,
                         leafReference),
                  entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }

    public static class PlaceholderCannotBeResolvedValidationError extends SimpleValidationError<CaseFieldEntity> {
        public PlaceholderCannotBeResolvedValidationError(String caseFieldReference, String placeholder, CaseFieldEntity entity) {
            super(format("Label of caseField '%s' has placeholder '%s' that points to unknown case field",
                         caseFieldReference,
                         placeholder),
                  entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
