package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventEntitySecurityClassificationValidatorImplTest {

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
    public void securityClassificationIsNull_InvalidValidationResultContainingEventEntityMissingSecurityClassificationValidationErrorReturned() {

        EventEntity eventEntity = new EventEntity();
        EventEntityValidationContext eventEntityValidationContext
            = eventEntityValidationContext(null, null);


        ValidationResult validationResult
            = new EventEntitySecurityClassificationValidatorImpl().validate(
                eventEntity,
                eventEntityValidationContext
        );

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().get(0)
            instanceof EventEntityMissingSecurityClassificationValidationError);
        assertEquals(
            eventEntity,
            ((EventEntityMissingSecurityClassificationValidationError) validationResult.getValidationErrors().get(0))
                .getEventEntity()
        );
    }

    private void assertCaseFieldWithSecurityClassificationIsValidInContextOfParent(SecurityClassification caseFieldSecurityClassification,
                                                                                   SecurityClassification parentSecurityClassification,
                                                                                   boolean isValid) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setSecurityClassification(caseFieldSecurityClassification);
        EventEntityValidationContext eventEntityValidationContext
            = eventEntityValidationContext("",parentSecurityClassification);

        ValidationResult validationResult
            = new EventEntitySecurityClassificationValidatorImpl().validate(eventEntity, eventEntityValidationContext);

        assertEquals(isValid, validationResult.isValid());
        assertEquals(isValid ? 0 : 1, validationResult.getValidationErrors().size());

        if (!isValid) {
            assertTrue(validationResult.getValidationErrors().get(0)
                instanceof EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError);

            assertEquals(
                eventEntity,
                ((EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) validationResult.getValidationErrors().get(0))
                    .getEventEntity()
            );
            assertEquals(
                eventEntityValidationContext,
                ((EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) validationResult.getValidationErrors().get(0))
                    .getEventEntityValidationContext()
            );
        }

    }

    private EventEntityValidationContext eventEntityValidationContext(String parentCaseName, SecurityClassification parentSecurityClassification) {
        EventEntityValidationContext eventEntityValidationContext = mock(EventEntityValidationContext.class);
        when(eventEntityValidationContext.getCaseName()).thenReturn(parentCaseName);
        when(eventEntityValidationContext.getParentSecurityClassification()).thenReturn(parentSecurityClassification);
        return eventEntityValidationContext;
    }
}
