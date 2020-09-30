package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComplexFieldEntitySecurityClassificationValidatorImplTest {

    private CaseFieldComplexFieldEntityValidator.ValidationContext complexFieldEntityValidationContext =
        mock(CaseFieldComplexFieldEntityValidator.ValidationContext.class);
    private static final String PREDEFINED_COMPLEX_TYPE = "PredefinedComplexType";

    private ComplexFieldEntitySecurityClassificationValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = new ComplexFieldEntitySecurityClassificationValidatorImpl();
    }


    @Test
    public void securityClassificationIsSet_relevantValidationResultReturned() {

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PUBLIC, null, true);

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PUBLIC, SecurityClassification.PUBLIC, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PRIVATE, SecurityClassification.PUBLIC, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.RESTRICTED, SecurityClassification.PUBLIC, true);

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PUBLIC, SecurityClassification.PRIVATE, false);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PRIVATE, SecurityClassification.PRIVATE, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.RESTRICTED, SecurityClassification.PRIVATE, true);

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PUBLIC, SecurityClassification.RESTRICTED, false);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.PRIVATE, SecurityClassification.RESTRICTED, false);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(new ComplexFieldEntity(),
            SecurityClassification.RESTRICTED, SecurityClassification.RESTRICTED, true);
    }

    @Test
    public void securityClassificationIsSetOnPredefinedComplexType_alwaysValid() {

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.PUBLIC, SecurityClassification.PUBLIC, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.PRIVATE, SecurityClassification.PUBLIC, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.RESTRICTED, SecurityClassification.PUBLIC, true);

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.PUBLIC, SecurityClassification.PRIVATE, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.PRIVATE, SecurityClassification.PRIVATE, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.RESTRICTED, SecurityClassification.PRIVATE, true);

        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.PUBLIC, SecurityClassification.RESTRICTED, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.PRIVATE, SecurityClassification.RESTRICTED, true);
        assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
            complexField(fieldType(PREDEFINED_COMPLEX_TYPE)),
            SecurityClassification.RESTRICTED, SecurityClassification.RESTRICTED, true);

    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void securityClassificationIsNull_invalidValidationResultContainingCaseFieldEntityMissingSecurityClassificationValidationErrorReturned() {

        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();

        ValidationResult validationResult
            = classUnderTest.validate(
            complexFieldEntity,
            complexFieldEntityValidationContext
        );

        assertValidationResultContainsComplexFieldEntityMissingSecurityClassificationValidationError(
            validationResult, complexFieldEntity);

        validationResult = classUnderTest.validate(complexFieldEntity);

        assertValidationResultContainsComplexFieldEntityMissingSecurityClassificationValidationError(
            validationResult, complexFieldEntity);
    }

    private void assertComplexFieldWithSecurityClassificationIsValidInContextOfParent(
        ComplexFieldEntity complexFieldEntity, SecurityClassification caseFieldSecurityClassification,
        SecurityClassification parentSecurityClassification, boolean isValid) {

        when(complexFieldEntityValidationContext.getParentSecurityClassification())
            .thenReturn(parentSecurityClassification);
        when(complexFieldEntityValidationContext.getPreDefinedComplexTypes())
            .thenReturn(Arrays.asList(fieldType(PREDEFINED_COMPLEX_TYPE)));

        complexFieldEntity.setSecurityClassification(caseFieldSecurityClassification);
        ValidationResult validationResult = classUnderTest.validate(
            complexFieldEntity, complexFieldEntityValidationContext);

        assertEquals(isValid, validationResult.isValid());
        assertEquals(isValid ? 0 : 1, validationResult.getValidationErrors().size());

        if (!isValid) {
            assertTrue(validationResult.getValidationErrors().get(0)
                instanceof ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError);

            assertEquals(
                complexFieldEntity,
                ((ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) validationResult
                    .getValidationErrors().get(0))
                    .getComplexFieldEntity()
            );
            assertEquals(
                complexFieldEntityValidationContext,
                ((ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) validationResult
                    .getValidationErrors().get(0))
                    .getComplexFieldEntityValidationContext()
            );
        }

    }

    private void assertValidationResultContainsComplexFieldEntityMissingSecurityClassificationValidationError(
        ValidationResult validationResult, ComplexFieldEntity complexFieldEntity) {

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().get(0)
            instanceof ComplexFieldEntityMissingSecurityClassificationValidationError);
        assertEquals(
            complexFieldEntity,
            ((ComplexFieldEntityMissingSecurityClassificationValidationError) validationResult
                .getValidationErrors().get(0))
                .getComplexFieldEntity()
        );

    }

    private ComplexFieldEntity complexField(FieldTypeEntity complexFieldType) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setComplexFieldType(complexFieldType);
        return complexFieldEntity;
    }

    private FieldTypeEntity fieldType(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

}
