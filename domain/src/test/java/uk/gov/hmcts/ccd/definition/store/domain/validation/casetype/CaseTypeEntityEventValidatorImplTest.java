package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

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
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class CaseTypeEntityEventValidatorImplTest {

    private static final String CASE_TYPE_NAME = "Case Type Name";

    private static final SecurityClassification CASE_TYPE_SECURITY_CLASSIFICATION = SecurityClassification.PRIVATE;

    @Mock
    private EventEntityValidator eventEntityValidator1;

    @Mock
    private EventEntityValidator eventEntityValidator2;

    @Captor
    private ArgumentCaptor<Collection<CaseFieldEntity>> captor;

    private EventEntity eventEntity1 = new EventEntity();

    private EventEntity eventEntity2 = new EventEntity();

    private EventEntity eventEntity3 = new EventEntity();

    private CaseTypeEntity caseType = caseType(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION,
        Arrays.asList(eventEntity1, eventEntity2, eventEntity3));

    private CaseTypeEntityEventValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(eventEntityValidator1.validate(any(), any())).thenReturn(new ValidationResult());
        when(eventEntityValidator2.validate(any(), any())).thenReturn(new ValidationResult());

        classUnderTest = new CaseTypeEntityEventValidatorImpl(
            Arrays.asList(eventEntityValidator1, eventEntityValidator2)
        );
    }

    @Test
    public void caseFieldsAllValid_allValidatorsCalledWithContextBuiltFromCaseType_EmptyValidationResultReturned() {

        ValidationResult validationResult = classUnderTest.validate(caseType);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getValidationErrors().isEmpty());

        verifyAllValidatorsCalledOnceForEachEventWithCorrectValidationContext();

    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void caseFields1And3AreInvalid_allValidatorsCalledWithContextBuiltFromCaseType_ValidationResultWithErrorsForCaseFieldEntity1And3Returned() {

        when(eventEntityValidator1.validate(eq(eventEntity1), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "eventEntityValidator1 failed for eventEntity1")));
        when(eventEntityValidator2.validate(eq(eventEntity3), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "eventEntityValidator3 failed for eventEntity3")));

        ValidationResult validationResult = classUnderTest.validate(caseType);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());

        assertThat(validationResult.getValidationErrors(), allOf(
            hasItem(matchesValidationErrorWithDefaultMessage("eventEntityValidator1 failed for eventEntity1")),
            hasItem(matchesValidationErrorWithDefaultMessage("eventEntityValidator3 failed for eventEntity3"))
            )
        );

        verifyAllValidatorsCalledOnceForEachEventWithCorrectValidationContext();

    }

    private CaseTypeEntity caseType(String caseTypeName,
                                    SecurityClassification securityClassification,
                                    Collection<EventEntity> events) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setName(caseTypeName);
        caseType.setSecurityClassification(securityClassification);
        caseType.addEvents(events);
        return caseType;
    }

    private void verifyAllValidatorsCalledOnceForEachEventWithCorrectValidationContext() {
        verify(eventEntityValidator1).validate(
            eq(eventEntity1),
            argThat(matchesEventEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(eventEntityValidator1).validate(
            eq(eventEntity2),
            argThat(matchesEventEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(eventEntityValidator1).validate(
            eq(eventEntity3),
            argThat(matchesEventEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );

        verify(eventEntityValidator2).validate(
            eq(eventEntity1),
            argThat(matchesEventEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(eventEntityValidator2).validate(
            eq(eventEntity2),
            argThat(matchesEventEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(eventEntityValidator2).validate(
            eq(eventEntity3),
            argThat(matchesEventEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
    }

    private <T> Matcher<T> matchesEventEntityValidationContext(String caseTypeName,
                                                               SecurityClassification securityClassification) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof EventEntityValidationContext
                    && ((EventEntityValidationContext) o).getCaseName() == caseTypeName
                    && ((EventEntityValidationContext) o).getParentSecurityClassification() == securityClassification;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    " a EventEntityValidationContext containing the reference to the Case Type Name "
                        + "and SecurityClassification of the CaseType");
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
