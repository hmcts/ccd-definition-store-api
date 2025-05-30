package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventEntityCreateEventValidatorTest {

    private EventEntityCreateEventValidator classUnderTest = new EventEntityCreateEventValidator();

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    void eventHasCanCreateTrueAndPostStateIsNull_validationResultContainingCreateEventDoesNotHavePostconditionValidationErrorReturned() {

        assertValidationForCanCreateWithPostState(true, null, false);
        assertValidationForCanCreateWithPostState(true, new StateEntity(), true);
        assertValidationForCanCreateWithPostState(false, null, true);
        assertValidationForCanCreateWithPostState(false, new StateEntity(), true);

    }

    private EventPostStateEntity createEventPostStateEntity(StateEntity stateEntity, EventEntity eventEntity) {
        if (stateEntity != null) {
            EventPostStateEntity eventPostStateEntity = new EventPostStateEntity();
            eventPostStateEntity.setPostStateReference(stateEntity.getReference());
            eventPostStateEntity.setEventEntity(eventEntity);
            return eventPostStateEntity;
        }
        return  null;
    }

    private void assertValidationForCanCreateWithPostState(boolean canCreate, StateEntity postState, boolean isValid) {

        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event Reference");
        eventEntity.setCanCreate(canCreate);
        eventEntity.addEventPostStates(Lists.newArrayList(createEventPostStateEntity(postState, eventEntity)));
        ValidationResult validationResult = classUnderTest.validate(eventEntity, null);

        assertEquals(isValid, validationResult.isValid());
        assertEquals(isValid ? 0 : 1, validationResult.getValidationErrors().size());
        if (!isValid) {
            ValidationError validationError = validationResult.getValidationErrors().get(0);
            assertTrue(validationError instanceof CreateEventDoesNotHavePostStateValidationError);
            assertSame(((CreateEventDoesNotHavePostStateValidationError) validationError)
                .getEventEntity(), eventEntity);
        }
    }


}
