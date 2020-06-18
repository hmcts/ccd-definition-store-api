package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class EventEntityEventCaseFieldsValidatorImplTest {

    @Mock
    private EventCaseFieldEntityValidator eventCaseFieldValidator1;

    @Mock
    private EventCaseFieldEntityValidator eventCaseFieldValidator2;

    @Mock
    private EventEntityValidationContext eventEntityValidationContext;

    @Captor
    private ArgumentCaptor<Collection<CaseFieldEntity>> captor;

    private EventCaseFieldEntity eventCaseFieldEntity1 = new EventCaseFieldEntity();

    private EventCaseFieldEntity eventCaseFieldEntity2 = new EventCaseFieldEntity();

    private EventCaseFieldEntity eventCaseFieldEntity3 = new EventCaseFieldEntity();

    private EventEntityEventCaseFieldsValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(eventCaseFieldValidator1.validate(any(), any())).thenReturn(new ValidationResult());
        when(eventCaseFieldValidator2.validate(any(), any())).thenReturn(new ValidationResult());
        when(eventEntityValidationContext.getCaseRoles()).thenReturn(new ArrayList<>());

        classUnderTest = new EventEntityEventCaseFieldsValidatorImpl(
            Arrays.asList(eventCaseFieldValidator1, eventCaseFieldValidator2)
        );
    }

    @Test
    public void eventCaseFieldsAllValid_allValidatorsCalledWithContextBuiltFromCaseType_EmptyValidationResultReturned() {


        ValidationResult validationResult = classUnderTest.validate(
            event(
                Arrays.asList(eventCaseFieldEntity1, eventCaseFieldEntity2, eventCaseFieldEntity3)
            ),
            eventEntityValidationContext
        );

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getValidationErrors().isEmpty());

        verifyAllValidatorsCalledOnceForEachCaseFieldWithCorrectValidationContext();

    }

    @Test
    public void eventCaseFields1And3AreInvalid_allValidatorsCalled_ValidationResultWithErrorsForEventCaseFields1And3Returned() {

        when(eventCaseFieldValidator1.validate(eq(eventCaseFieldEntity1), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage("eventCaseFieldValidator1 failed for eventCaseFieldEntity1")));
        when(eventCaseFieldValidator2.validate(eq(eventCaseFieldEntity3), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage("eventCaseFieldValidator2 failed for eventCaseFieldEntity3")));

        ValidationResult validationResult = classUnderTest.validate(
            event(
                Arrays.asList(eventCaseFieldEntity1, eventCaseFieldEntity2, eventCaseFieldEntity3)
            ),
            eventEntityValidationContext
        );

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());

        assertThat(validationResult.getValidationErrors(), allOf(
            hasItem(matchesValidationErrorWithDefaultMessage("eventCaseFieldValidator1 failed for eventCaseFieldEntity1")),
            hasItem(matchesValidationErrorWithDefaultMessage("eventCaseFieldValidator2 failed for eventCaseFieldEntity3"))
            )
        );

        verifyAllValidatorsCalledOnceForEachCaseFieldWithCorrectValidationContext();

    }

    private EventEntity event(Collection<EventCaseFieldEntity> eventCaseFields) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.addEventCaseFields(eventCaseFields);
        return eventEntity;
    }

    private void verifyAllValidatorsCalledOnceForEachCaseFieldWithCorrectValidationContext() {
        verify(eventCaseFieldValidator1).validate(
            eq(eventCaseFieldEntity1),
            argThat(matchesExpectedEventCaseFieldEntityValidationContext())
        );
        verify(eventCaseFieldValidator1).validate(
            eq(eventCaseFieldEntity2),
            argThat(matchesExpectedEventCaseFieldEntityValidationContext())
        );
        verify(eventCaseFieldValidator1).validate(
            eq(eventCaseFieldEntity3),
            argThat(matchesExpectedEventCaseFieldEntityValidationContext())
        );

        verify(eventCaseFieldValidator2).validate(
            eq(eventCaseFieldEntity1),
            argThat(matchesExpectedEventCaseFieldEntityValidationContext())
        );
        verify(eventCaseFieldValidator2).validate(
            eq(eventCaseFieldEntity2),
            argThat(matchesExpectedEventCaseFieldEntityValidationContext())
        );
        verify(eventCaseFieldValidator2).validate(
            eq(eventCaseFieldEntity3),
            argThat(matchesExpectedEventCaseFieldEntityValidationContext())
        );
    }

    private org.hamcrest.Matcher<EventCaseFieldEntityValidationContext> matchesExpectedEventCaseFieldEntityValidationContext() {
        return new BaseMatcher<EventCaseFieldEntityValidationContext>() {
            @Override
            public boolean matches(Object o) {
                List<EventCaseFieldEntity> expectedExisitingEventCaseFieldEntities
                    = Arrays.asList(eventCaseFieldEntity1, eventCaseFieldEntity2, eventCaseFieldEntity3);

                return o instanceof EventCaseFieldEntityValidationContext
                    && ((EventCaseFieldEntityValidationContext) o).getAllEventCaseFieldEntitiesForEventCase().size()
                        == expectedExisitingEventCaseFieldEntities.size()
                            && ((EventCaseFieldEntityValidationContext) o).getAllEventCaseFieldEntitiesForEventCase()
                                .containsAll(expectedExisitingEventCaseFieldEntities);

            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a EventCaseFieldEntityValidationContext containing all the EventCaseFields for the event");
            }
        };
    }

    private <T> Matcher<T> matchesValidationErrorWithDefaultMessage(String defaultMessage) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof ValidationError
                    && ((ValidationError) o).getDefaultMessage().equals(defaultMessage);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a ValidationError with defaultMessage " + defaultMessage);
            }
        };
    }

    private ValidationResult validationResultWithError(ValidationError validationError) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(validationError);
        return validationResult;
    }

    private ValidationError validationErrorWithDefaultMessage(String defaultMessage) {
        return new TestValidationError(defaultMessage);
    }

}
