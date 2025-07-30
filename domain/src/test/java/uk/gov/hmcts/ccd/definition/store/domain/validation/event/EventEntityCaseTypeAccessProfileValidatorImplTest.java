package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventEntityCaseTypeAccessProfileValidatorImplTest {

    private final EventEntityCaseTypeAccessProfileValidatorImpl validator
            = new EventEntityCaseTypeAccessProfileValidatorImpl();

    @Test
    @DisplayName(
        "Should return validation result with exception when there is more than one event for a CaseType associated "
            + "with user 'caseworker-caa'")
    void shouldReturnValidationResultWithError_whenMultipleEventsWithCaseTypeAndUserCaseworkerCaa() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType_example");
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(caseType);
        EventEntity eventEntity1 = new EventEntity();
        eventEntity1.setCaseType(caseType);
        EventACLEntity eventACLEntity1 = new EventACLEntity();
        EventEntity eventEntity2 = new EventEntity();
        eventEntity2.addEventACL(eventACLEntity1);
        eventACLEntity1.setEventEntity(eventEntity);
        eventACLEntity1.setAccessProfileId("caseworker-caa");
        EventACLEntity eventACLEntity2 = new EventACLEntity();
        eventEntity2.addEventACL(eventACLEntity2);
        eventACLEntity2.setEventEntity(eventEntity1);
        eventACLEntity2.setAccessProfileId("Caseworker-caa");

        EventEntityValidationContext context = mock(EventEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(eventEntity2, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(EventEntityCaseTypeAccessProfileValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("AccessProfile 'Caseworker-caa' is defined more than once for case type 'CaseType_example'"));
    }

    @Test
    @DisplayName(
        "Should return validation result with exception when there is more than one event for a CaseType associated "
            + "with user 'caseworker-approver'")
    void shouldReturnValidationResultWithError_whenMultipleEventsWithCaseTypeAndUserCaseworkerApprover() {

        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType_example");
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(caseType);
        EventEntity eventEntity1 = new EventEntity();
        eventEntity1.setCaseType(caseType);
        EventACLEntity eventACLEntity1 = new EventACLEntity();
        EventEntity eventEntity2 = new EventEntity();
        eventEntity2.addEventACL(eventACLEntity1);
        eventACLEntity1.setEventEntity(eventEntity);
        eventACLEntity1.setAccessProfileId("caseworker-approver");
        EventACLEntity eventACLEntity2 = new EventACLEntity();
        eventEntity2.addEventACL(eventACLEntity2);
        eventACLEntity2.setEventEntity(eventEntity1);
        eventACLEntity2.setAccessProfileId("Caseworker-approver");

        EventEntityValidationContext context = mock(EventEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(eventEntity2, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(EventEntityCaseTypeAccessProfileValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("AccessProfile 'Caseworker-approver' is defined more than once for case type 'CaseType_example'"));
    }

    @Test
    @DisplayName(
        "Should return validation result with no exception when there is more than one event for different CaseTypes "
            + "associated with user 'caseworker-approver'")
    void shouldReturnValidationResultWithoutError_whenMultipleEventsWithDifferentCaseTypeAndUserCaseworkerApprover() {

        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType_example");
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(caseType);
        CaseTypeEntity caseType1 = new CaseTypeEntity();
        caseType1.setReference("CaseType_example_2");
        EventEntity eventEntity1 = new EventEntity();
        eventEntity1.setCaseType(caseType1);
        EventACLEntity eventACLEntity1 = new EventACLEntity();
        EventEntity eventEntity2 = new EventEntity();
        eventEntity2.addEventACL(eventACLEntity1);
        eventACLEntity1.setEventEntity(eventEntity);
        eventACLEntity1.setAccessProfileId("caseworker-approver");
        EventACLEntity eventACLEntity2 = new EventACLEntity();
        eventEntity2.addEventACL(eventACLEntity2);
        eventACLEntity2.setEventEntity(eventEntity1);
        eventACLEntity2.setAccessProfileId("Caseworker-approver");

        EventEntityValidationContext context = mock(EventEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(eventEntity2, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }

    @Test
    @DisplayName(
        "Should return validation result with no exception when there is more than one event for CaseTypes not "
            + "associated with either user 'caseworker-approver' or 'caseworker-caa'")
    void shouldReturnValidationResultWithoutError_whenMultipleEventsWithDifferentCaseTypeAndNotSpecificUsers() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType_example");
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(caseType);
        EventEntity eventEntity1 = new EventEntity();
        eventEntity1.setCaseType(caseType);
        EventACLEntity eventACLEntity1 = new EventACLEntity();
        EventEntity eventEntity2 = new EventEntity();
        eventEntity2.addEventACL(eventACLEntity1);
        eventACLEntity1.setEventEntity(eventEntity);
        eventACLEntity1.setAccessProfileId("caseworker");
        EventACLEntity eventACLEntity2 = new EventACLEntity();
        eventEntity2.addEventACL(eventACLEntity2);
        eventACLEntity2.setEventEntity(eventEntity1);
        eventACLEntity2.setAccessProfileId("Caseworker");

        EventEntityValidationContext context = mock(EventEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(eventEntity2, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }

    @Test
    @DisplayName(
        "Should return validation result with no exception when there is one event for CaseTypes associated "
            + "with user 'caseworker-caa'")
    void shouldReturnValidationResultWithoutError_whenOneEventWithCaseTypeAndUserCaseworkerCaa() {

        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType_example");
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(caseType);
        EventACLEntity eventACLEntity1 = new EventACLEntity();
        EventEntity eventEntity2 = new EventEntity();
        eventEntity2.addEventACL(eventACLEntity1);
        eventACLEntity1.setEventEntity(eventEntity);
        eventACLEntity1.setAccessProfileId("caseworker-caa");

        EventEntityValidationContext context = mock(EventEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(eventEntity2, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }

}
