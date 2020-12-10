package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class EventCaseFieldPublishValidatorImplTest {

    private EventCaseFieldPublishValidatorImpl classUnderTest = new EventCaseFieldPublishValidatorImpl();
    final List<String> caseRoles = new ArrayList<>();
    final EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
    final List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase = new ArrayList();
    final String eventId = "eventId";

    final EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
        new EventCaseFieldEntityValidationContext(eventId, allEventCaseFieldEntitiesForEventCase, caseRoles);

    @Before
    public void setUp() {
        caseRoles.add("ROLE1");
        caseRoles.add("ROLE2");
    }

    private void setupEventCaseFieldEntity() {
        final CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        final EventEntity eventEntity = new EventEntity();
        caseFieldEntity.setReference("caseFieldReference");
        eventEntity.setName(eventId);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        eventCaseFieldEntity.setPublish(false);
        eventEntity.setPublish(false);
        eventCaseFieldEntity.setEvent(eventEntity);
    }

    private void addPublishAsInEventComplexTypeEntity(String publishAsEventComplexType,
                                                      String publishAsEventCaseFieldEntity) {

        final EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        final EventComplexTypeEntity eventComplexTypeEntity = new EventComplexTypeEntity();
        final List<EventComplexTypeEntity> eventComplexTypeEntities = new ArrayList<>();
        eventComplexTypeEntity.setPublishAs(publishAsEventComplexType);
        eventCaseFieldEntity.setPublishAs(publishAsEventCaseFieldEntity);
        eventComplexTypeEntities.add(eventComplexTypeEntity);
        eventCaseFieldEntity.addComplexFields(eventComplexTypeEntities);
        allEventCaseFieldEntitiesForEventCase.add(eventCaseFieldEntity);
    }

    @Test
    public void should_pass_due_to_correct_publishAs() {
        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test", null);
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    public void should_pass_due_to_null_publishAs() {

        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs(null);
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    public void should_fail_due_to_incorrect_publishAs() {

        final String expectedError = "PublishAs column cannot have spaces, reference 'caseFieldReference'";
        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs("Test Test");
        assertForFailTest(expectedError);
    }

    @Test
    public void should_fail_due_to_duplicated_publishAs_in_EventComplexTypeEntity() {

        final String expectedError = "PublishAs column has an invalid value 'Test',  reference 'caseFieldReference'. "
            + "This value must be unique across CaseEventToFields and EventToComplexTypes for the case type. ";

        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test", null);
        addPublishAsInEventComplexTypeEntity("Test", null);

        assertForFailTest(expectedError);
    }


    @Test
    public void should_fail_due_to_duplicated_publishAs_in_EventCaseFieldEntity() {

        final String expectedError = "PublishAs column has an invalid value 'Test',  reference 'caseFieldReference'. "
            + "This value must be unique across CaseEventToFields and EventToComplexTypes for the case type. ";

        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test", null);
        addPublishAsInEventComplexTypeEntity(null, "Test");

        assertForFailTest(expectedError);
    }

    @Test
    public void should_fail_due_to_duplicated_publishAs_in_EventCaseFieldEntity_and_EventComplexTypeEntity() {

        final String expectedError = "PublishAs column has an invalid value 'Test',  reference 'caseFieldReference'. "
            + "This value must be unique across CaseEventToFields and EventToComplexTypes for the case type. ";

        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test", null);
        addPublishAsInEventComplexTypeEntity("Test", "Test");

        assertForFailTest(expectedError);
    }

    @Test
    public void should_fail_due_to_EventPublishFalse_and_EventCaseFieldEntityPublishTrue() {

        final String expectedError = "Publish column has an invalid value 'true',  reference 'caseFieldReference'. "
            + "If the Event is set to false, CaseEventToFields and EventToComplexTypes cannot have Publish columns as "
            + "true for the case type.";

        setupEventCaseFieldEntity();
        eventCaseFieldEntity.setPublishAs("Test");
        eventCaseFieldEntity.setPublish(true);
        addPublishAsInEventComplexTypeEntity("Test", null);
        assertForFailTest(expectedError);
    }

    private void assertForFailTest(String expectedError) {
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertEquals(validationResult.getValidationErrors().get(0).getDefaultMessage(),expectedError);
    }
}
