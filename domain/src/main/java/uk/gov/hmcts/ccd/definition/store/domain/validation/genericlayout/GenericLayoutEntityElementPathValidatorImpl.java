package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class GenericLayoutEntityElementPathValidatorImpl implements GenericLayoutValidator {

    private static final String ERROR_MESSAGE_INVALID_PATH =
        "Invalid ListElementCode '%s' for case type '%s', case field '%s' with label '%s'";
    private static final String ERROR_MESSAGE_PATH_DEFINED_FOR_NON_COMPLEX_FIELD =
        "ListElementCode '%s' can be only defined for complex fields. Case Field '%s', case type '%s'";

    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public GenericLayoutEntityElementPathValidatorImpl(CaseFieldEntityUtil caseFieldEntityUtil) {
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(GenericLayoutEntity entity, List<GenericLayoutEntity> allGenericLayouts) {
        final ValidationResult validationResult = new ValidationResult();

        validatePaths(entity, validationResult);

        return validationResult;
    }

    private void validatePaths(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseField() != null && entity.getCaseType() != null
            && isNotBlank(entity.getCaseFieldElementPath())) {
            if (entity.getCaseField().isComplexFieldType() || entity.getCaseField().isCollectionFieldType()) {
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
}
