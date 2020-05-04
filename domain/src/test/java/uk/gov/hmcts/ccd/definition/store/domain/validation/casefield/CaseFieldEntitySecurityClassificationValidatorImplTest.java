package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CaseFieldEntitySecurityClassificationValidatorImplTest {

    @Test
    public void securityClassificationIsSet_relevantValidationResultReturned() {

        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PUBLIC, null, true);

        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PUBLIC, SecurityClassification.PUBLIC, true);
        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PRIVATE, SecurityClassification.PUBLIC, true);
        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.RESTRICTED, SecurityClassification.PUBLIC, true);

        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PUBLIC, SecurityClassification.PRIVATE, false);
        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PRIVATE, SecurityClassification.PRIVATE, true);
        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.RESTRICTED, SecurityClassification.PRIVATE, true);

        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PUBLIC, SecurityClassification.RESTRICTED, false);
        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.PRIVATE, SecurityClassification.RESTRICTED, false);
        assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification.RESTRICTED, SecurityClassification.RESTRICTED, true);
    }

    @Test
    public void securityClassificationIsNull_invalidValidationResultContainingCaseFieldEntityMissingSecurityClassificationValidationErrorReturned() {

        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        CaseFieldEntityValidationContext caseFieldEntityValidationContext
            = caseFieldEntityValidationContext(null, null);


        ValidationResult validationResult
            = new CaseFieldEntitySecurityClassificationValidatorImpl().validate(
                caseFieldEntity,
                caseFieldEntityValidationContext
        );

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().get(0)
            instanceof CaseFieldEntityMissingSecurityClassificationValidationError);
        assertEquals(
            caseFieldEntity,
            ((CaseFieldEntityMissingSecurityClassificationValidationError) validationResult.getValidationErrors().get(0))
                .getCaseFieldEntity()
        );
        assertEquals(
            caseFieldEntityValidationContext,
            ((CaseFieldEntityMissingSecurityClassificationValidationError) validationResult.getValidationErrors().get(0))
                .getCaseFieldEntityValidationContext()
        );
    }

    private void assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification caseFieldSecurityClassification,
                                                                                   SecurityClassification parentSecurityClassification,
                                                                                   boolean isValid) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setSecurityClassification(caseFieldSecurityClassification);
        CaseFieldEntityValidationContext caseFieldEntityValidationContext
            = caseFieldEntityValidationContext("",parentSecurityClassification);
        ValidationResult validationResult
            = new CaseFieldEntitySecurityClassificationValidatorImpl().validate(caseFieldEntity, caseFieldEntityValidationContext);

        assertEquals(isValid, validationResult.isValid());
        assertEquals(isValid ? 0 : 1, validationResult.getValidationErrors().size());

        if (!isValid) {
            assertTrue(validationResult.getValidationErrors().get(0)
                instanceof CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError);

            assertEquals(
                caseFieldEntity,
                ((CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) validationResult.getValidationErrors().get(0))
                    .getCaseFieldEntity()
            );
            assertEquals(
                caseFieldEntityValidationContext,
                ((CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) validationResult.getValidationErrors().get(0))
                    .getCaseFieldEntityValidationContext()
            );
        }

    }

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext(String parentCaseName,
                                                                              SecurityClassification parentCaseFieldSecurityClassification) {
        CaseFieldEntityValidationContext caseFieldEntityValidationContext = mock(CaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getCaseName()).thenReturn(parentCaseName);
        when(caseFieldEntityValidationContext.getParentSecurityClassification()).thenReturn(parentCaseFieldSecurityClassification);
        return caseFieldEntityValidationContext;
    }

}
