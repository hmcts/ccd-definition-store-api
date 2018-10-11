package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventEntityCreateEventValidatorTest {

    private EventEntityCreateEventValidator classUnderTest = new EventEntityCreateEventValidator();

    @Test
    public void eventHasCanCreateTrueAndPostStateIsNull_validationResultContainingCreateEventDoesNotHavePostconditionValidationErrorReturned() {

        assertValidationForCanCreateWithPostState(true, null, false);
        assertValidationForCanCreateWithPostState(true, new StateEntity(), true);
        assertValidationForCanCreateWithPostState(false, null, true);
        assertValidationForCanCreateWithPostState(false, new StateEntity(), true);

    }

    private void assertValidationForCanCreateWithPostState(boolean canCreate, StateEntity postState, boolean isValid) {

        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event Reference");
        eventEntity.setPostState(postState);
        eventEntity.setCanCreate(canCreate);
        ValidationResult validationResult = classUnderTest.validate(eventEntity, null);

        assertEquals(isValid, validationResult.isValid());
        assertEquals(isValid ? 0 : 1, validationResult.getValidationErrors().size());
        if (!isValid) {
            ValidationError validationError = validationResult.getValidationErrors().get(0);
            assertTrue(validationError instanceof CreateEventDoesNotHavePostStateValidationError);
            assertTrue(((CreateEventDoesNotHavePostStateValidationError) validationError).getEventEntity() == eventEntity);
        }
    }


}
