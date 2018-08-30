package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EventCaseFieldShowConditionValidatorImplTest {

    @Mock
    private ShowConditionParser showConditionExtractor;

    @InjectMocks
    private EventCaseFieldShowConditionValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void anotherEventCaseFieldExistsWithSameCaseFieldIdAsEventFieldEntityWithShowConditionSet_validValidationResultReturned()
        throws InvalidShowConditionException {

        String matchingCaseFieldId = "MatchingCaseFieldId";
        String showCondition = matchingCaseFieldId + "=true";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(matchingCaseFieldId).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = new EventCaseFieldEntityValidationContext(
            "EventId",
            Arrays.asList(
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId1"),
                    null
                ),
                eventCaseFieldEntityWithShowCondition,
                eventCaseFieldEntity(
                    caseFieldEntity(matchingCaseFieldId),
                    null
                ),
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId2"),
                    null
                ),
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId3"),
                    null
                )
            )
        );

        assertTrue(classUnderTest.validate(eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext)
                       .isValid());

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));
    }

    @Test
    public void noOtherEventCaseFieldExistsWithSameCaseFieldIdAsEventFieldEntityWithShowConditionSet_invalidValidationResultReturned()
        throws InvalidShowConditionException {

        String matchingCaseFieldId = "MatchingCaseFieldId";
        String showCondition = matchingCaseFieldId + "=true";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(matchingCaseFieldId).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = new EventCaseFieldEntityValidationContext(
            "EventId",
            Arrays.asList(
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId1"),
                    null
                ),
                eventCaseFieldEntityWithShowCondition,
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId2"),
                    null
                ),
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId3"),
                    null
                )
            )
        );

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext);

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));

        assertFalse(validationResult.isValid());

        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(validationResult.getValidationErrors().get(0) instanceof EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError);

    }

    @Test
    public void eventCaseFieldEntityWithNullShowCondition_validValidationResultReturned() {

        EventCaseFieldEntity eventCaseFieldEntityWithNullShowCondition = eventCaseFieldEntity(
            null,
            null
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = new EventCaseFieldEntityValidationContext(
            "EventId",
            Collections.emptyList()
        );

        assertTrue(classUnderTest.validate(eventCaseFieldEntityWithNullShowCondition, eventCaseFieldEntityValidationContext)
                       .isValid());

        verifyZeroInteractions(showConditionExtractor);
    }

    @Test
    public void eventCaseFieldEntityWithBlankShowCondition_validValidationResultReturned() {

        EventCaseFieldEntity eventCaseFieldEntityWithBlankShowCondition = eventCaseFieldEntity(
            null,
            "     "
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = new EventCaseFieldEntityValidationContext(
            "EventId",
            Collections.emptyList()
        );

        assertTrue(classUnderTest.validate(eventCaseFieldEntityWithBlankShowCondition, eventCaseFieldEntityValidationContext)
                       .isValid());

        verifyZeroInteractions(showConditionExtractor);

    }

    @Test
    public void invalidShowConditionExceptionThrown_validValidationResultReturned()
        throws InvalidShowConditionException {

        String showCondition = "InvalidShowCondition";
        EventCaseFieldEntity eventCaseFieldEntityWithInvalidShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = new EventCaseFieldEntityValidationContext(
            null, null
        );

        when(showConditionExtractor.parseShowCondition(any())).thenThrow(new InvalidShowConditionException(null));

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntityWithInvalidShowCondition, eventCaseFieldEntityValidationContext);

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));

        assertFalse(validationResult.isValid());

        assertEquals(1, validationResult.getValidationErrors().size());
        Assert.assertTrue(validationResult.getValidationErrors().get(0) instanceof EventCaseFieldEntityInvalidShowConditionError);

    }

    @Test
    public void shouldReturnInvalidResultWhenAnyCaseFieldUsedInAndConditionsDoNotMatchEventCaseFields() throws InvalidShowConditionException {

        String matchingCaseFieldId1 = "MatchingCaseFieldId1";
        String matchingCaseFieldId2 = "MatchingCaseFieldId2";
        String showCondition = matchingCaseFieldId1 + "=true AND " + matchingCaseFieldId2 + "=true";

        EventCaseFieldEntity eventCaseFieldEntityWithShowCondition = eventCaseFieldEntity(
            null,
            showCondition
        );

        when(showConditionExtractor.parseShowCondition(any())).thenReturn(
            new ShowCondition.Builder().field(matchingCaseFieldId1).field(matchingCaseFieldId2).build()
        );

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = new EventCaseFieldEntityValidationContext(
            "EventId",
            Arrays.asList(
                eventCaseFieldEntity(
                    caseFieldEntity("MatchingCaseFieldId1"),
                    null
                ),
                eventCaseFieldEntityWithShowCondition,
                eventCaseFieldEntity(
                    caseFieldEntity("NonMatchingCaseFieldId2"),
                    null
                )
            )
        );

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntityWithShowCondition, eventCaseFieldEntityValidationContext);

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getValidationErrors(), hasSize(1));
        assertThat(validationResult.getValidationErrors().get(0), instanceOf(EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError.class));

        verify(showConditionExtractor).parseShowCondition(eq(showCondition));
    }

    private EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseFieldEntity, String showCondition) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setEvent(new EventEntity());
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        eventCaseFieldEntity.setShowCondition(showCondition);
        return eventCaseFieldEntity;
    }

    private CaseFieldEntity caseFieldEntity(String caseFieldReference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(caseFieldReference);
        return caseFieldEntity;
    }
}
