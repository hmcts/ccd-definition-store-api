package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import java.util.Arrays;
import java.util.regex.Pattern;

@Component
public class CaseFieldEntityMetadataFieldValidatorImpl implements CaseFieldEntityValidator {

    private static final Pattern METADATA_PATTERN = Pattern.compile("\\[[^\\[]*\\]");

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        if (!caseField.isMetadataField() && METADATA_PATTERN.matcher(caseField.getReference()).matches()) {
            validationResult.addError(
                new CaseFieldEntityInvalidMetadataFieldValidationError(String.format("Invalid metadata field '%s' declaration for case type '%s'",
                                                                                     caseField.getReference(),
                                                                                     context.getCaseReference()), caseField, context)
            );
        }

        if (!caseField.isMetadataField() && Arrays.stream(MetadataField.values())
            .anyMatch(metadataField -> metadataField.name().equalsIgnoreCase(caseField.getReference())
            )) {
            validationResult.addError(
                new CaseFieldEntityInvalidMetadataFieldValidationError(String.format("Invalid case field reference name '%s' for case type '%s'."
                                                                                         + " This case field reference is reserved for metadata fields only.",
                                                                                     caseField.getReference(),
                                                                                     context.getCaseReference()), caseField, context)
            );
        }

        return validationResult;
    }
}
