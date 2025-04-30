package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaseTypeEntitySecurityClassificationValidatorImplTest {

    @Test
    public void securityClassificationIsSet_validValidationResultReturned() {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("WithSecurityClassification");
        caseTypeEntity.setSecurityClassification(SecurityClassification.PUBLIC);

        ValidationResult validationResult
            = new CaseTypeEntitySecurityClassificationValidatorImpl().validate(caseTypeEntity);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void securityClassificationIsNull_invalidValidationResultContainingCaseTypeEntityMissingSecurityClassificationValidationErrorReturned() {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("WithoutSecurityClassificationOrSecurityClassificationString");

        ValidationResult validationResult
            = new CaseTypeEntitySecurityClassificationValidatorImpl().validate(caseTypeEntity);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().get(0)
            instanceof CaseTypeEntityMissingSecurityClassificationValidationError);
        assertEquals(
            caseTypeEntity,
            ((CaseTypeEntityMissingSecurityClassificationValidationError) validationResult
                .getValidationErrors().get(0)).getCaseTypeEntity()
        );
    }

}
