package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import java.util.regex.Pattern;

@Component
public class CaseFieldEntityMetadataFieldValidatorImpl implements CaseFieldEntityValidator {

    private static final Pattern METADATA_PATTERN = Pattern.compile("\\[[^\\[]*\\]");

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();

        if (!caseField.isMetadataField() && METADATA_PATTERN.matcher(caseField.getReference()).matches()) {
            validationResult.addError(
                new CaseFieldEntityInvalidMetadataFieldValidationError(caseField, caseFieldEntityValidationContext)
            );
        }

        return validationResult;
    }
}
