package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityDefaultValueValidatorImpl.ORGANISATION_POLICY_ROLE;

public class EventComplexTypeEntityDefaultValueValidatorImplTest {

    private EventComplexTypeEntityDefaultValueValidatorImpl classUnderTest = new EventComplexTypeEntityDefaultValueValidatorImpl();
    final List<String> caseRoles = new ArrayList<>();
    final EventComplexTypeEntity eventCaseFieldEntity = new EventComplexTypeEntity();
    final List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase = new ArrayList();
    private static final String ROLE1 = "ROLE1";
    private static final String ROLE2 = "ROLE2";
    final String eventId = "eventId";
    private static final String GLOBAL_ROLE_COLLABORATOR = "[COLLABORATOR]";

    final EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
            new EventCaseFieldEntityValidationContext(eventId, allEventCaseFieldEntitiesForEventCase, caseRoles);

    @Before
    public void setUp() {
        eventCaseFieldEntity.setReference(ORGANISATION_POLICY_ROLE);
        caseRoles.add(ROLE1);
        caseRoles.add(ROLE2);
    }

    @Test
    public void should_pass_validation() {

        eventCaseFieldEntity.setDefaultValue(ROLE2);
        final ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    public void should_pass_validation_complex_field_reference() {
        eventCaseFieldEntity.setReference("TestComplexField." + ORGANISATION_POLICY_ROLE);
        eventCaseFieldEntity.setDefaultValue(ROLE2);
        final ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    public void should_pass_validation_due_to_global_role() {

        eventCaseFieldEntity.setDefaultValue(GLOBAL_ROLE_COLLABORATOR);
        final ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }


    @Test
    public void should_not_pass_validation_due_to_incorrect_role() {

        eventCaseFieldEntity.setDefaultValue("xxx");
        final ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().get(0).getDefaultMessage().contains("is not a valid role for"));
    }
}
