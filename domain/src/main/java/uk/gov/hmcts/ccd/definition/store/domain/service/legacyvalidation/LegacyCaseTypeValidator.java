package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

@Service
/**
 * @deprecated
// This component is deprecated; new validation should be part of the package
// uk.gov.hmcts.ccd.definition.store.domain.validation
// and new validators should be re-written to replace the validators called by this class
 */
@Deprecated
public class LegacyCaseTypeValidator {

    private final List<ValidationRule> rules;

    @Autowired
    public LegacyCaseTypeValidator(List<ValidationRule> rules) {
        this.rules = rules;
    }

    /**
     * Run all validation rules for the Case Type and collate all errors in the result
     *
     * @param caseTypeItem - Case Type to be validated
     */
    public void validateCaseType(CaseTypeEntity caseTypeEntity) {
        // If the Case Type Item has no Case Type then do not continue with validation
        if (caseTypeEntity == null)
            throw new CaseTypeValidationException(new CaseTypeValidationResult("No Case Type provided"));

        // Perform validation
        CaseTypeValidationResult result = new CaseTypeValidationResult();
        for (ValidationRule rule : this.rules) {
            String ruleResult = rule.validate(caseTypeEntity);
            if (ruleResult != null)
                result.addError(ruleResult);
        }

        if (!result.validationPassed())
            throw new CaseTypeValidationException(result);
    }
}
