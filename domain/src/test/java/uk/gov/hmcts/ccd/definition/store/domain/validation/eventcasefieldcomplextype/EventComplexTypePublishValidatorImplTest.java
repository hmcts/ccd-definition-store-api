package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class EventComplexTypePublishValidatorImplTest {

    private EventComplexTypePublishValidatorImpl classUnderTest = new EventComplexTypePublishValidatorImpl();
    final List<String> caseRoles = new ArrayList<>();
    final EventComplexTypeEntity eventComplexTypeEntity = new EventComplexTypeEntity();
    final List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase = new ArrayList();
    final String eventId = "eventId";

    final EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
        new EventCaseFieldEntityValidationContext(eventId, allEventCaseFieldEntitiesForEventCase, caseRoles);

    @Before
    public void setUp() {
        eventComplexTypeEntity.setReference("reference");
        caseRoles.add("ROLE1");
        caseRoles.add("ROLE2");
    }

    private void setupEventCaseFieldEntity() {
        final EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        final CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        final EventEntity eventEntity = new EventEntity();
        eventEntity.setName(eventId);
        eventCaseFieldEntity.setPublish(false);
        eventEntity.setPublish(false);
        eventComplexTypeEntity.setPublish(false);
        caseFieldEntity.setReference("caseFieldReference");
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        eventCaseFieldEntity.setEvent(eventEntity);
        eventComplexTypeEntity.setComplexFieldType(eventCaseFieldEntity);
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
        eventComplexTypeEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test",null);
        final ValidationResult validationResult = classUnderTest.validate(
            eventComplexTypeEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    public void should_pass_due_to_null_publishAs() {

        setupEventCaseFieldEntity();
        eventComplexTypeEntity.setPublishAs(null);
        final ValidationResult validationResult = classUnderTest.validate(
            eventComplexTypeEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    public void should_fail_due_to_incorrect_publishAs() {

        final String expectedError = "PublishAs column cannot have spaces, reference 'caseFieldReference.reference'";
        setupEventCaseFieldEntity();
        eventComplexTypeEntity.setPublishAs("Test Test");
        assertForFailTest(expectedError);
    }

    @Test
    public void should_fail_due_to_duplicated_publishAs_in_EventComplexTypeEntity() {

        final String expectedError = "PublishAs column has an invalid value 'Test',  reference "
            + "'caseFieldReference.reference'. This value must be unique across "
            + "CaseEventToFields and EventToComplexTypes for the case type. ";

        setupEventCaseFieldEntity();
        eventComplexTypeEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test",null);
        addPublishAsInEventComplexTypeEntity("Test",null);

        assertForFailTest(expectedError);
    }


    @Test
    public void should_fail_due_to_duplicated_publishAs_in_EventCaseFieldEntity() {

        final String expectedError = "PublishAs column has an invalid value 'Test',  reference "
            + "'caseFieldReference.reference'. This value must be unique across "
            + "CaseEventToFields and EventToComplexTypes for the case type. ";

        setupEventCaseFieldEntity();
        eventComplexTypeEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test",null);
        addPublishAsInEventComplexTypeEntity(null,"Test");

        assertForFailTest(expectedError);
    }

    @Test
    public void should_fail_due_to_duplicated_publishAs_in_EventCaseFieldEntity_and_EventComplexTypeEntity() {

        final String expectedError = "PublishAs column has an invalid value 'Test',  reference "
            + "'caseFieldReference.reference'. This value must be unique across "
            + "CaseEventToFields and EventToComplexTypes for the case type. ";

        setupEventCaseFieldEntity();
        eventComplexTypeEntity.setPublishAs("Test");
        addPublishAsInEventComplexTypeEntity("Test",null);
        addPublishAsInEventComplexTypeEntity("Test","Test");

        assertForFailTest(expectedError);
    }

    @Test
    public void should_fail_due_to_EventPublishFalse_and_EventComplexTypePublishTrue() {

        final String expectedError = "Publish column has an invalid value 'true',  reference "
            + "'caseFieldReference.reference'. If the Event is set to false, CaseEventToFields and EventToComplexTypes "
            + "cannot have Publish columns as true for the case type.";

        setupEventCaseFieldEntity();
        eventComplexTypeEntity.setPublishAs("Test");
        eventComplexTypeEntity.setPublish(true);
        addPublishAsInEventComplexTypeEntity("Test",null);

        assertForFailTest(expectedError);
    }

    private void assertForFailTest(String expectedError) {
        final ValidationResult validationResult = classUnderTest.validate(
            eventComplexTypeEntity, eventCaseFieldEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertEquals(validationResult.getValidationErrors().get(0).getDefaultMessage(),expectedError);
    }
}
