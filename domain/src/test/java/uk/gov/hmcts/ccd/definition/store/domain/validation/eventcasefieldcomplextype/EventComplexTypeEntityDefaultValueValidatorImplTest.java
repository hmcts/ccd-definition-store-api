package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityDefaultValueValidatorImpl.ORGANISATION_POLICY_ROLE;

class EventComplexTypeEntityDefaultValueValidatorImplTest {

    private EventComplexTypeEntityDefaultValueValidatorImpl classUnderTest =
        new EventComplexTypeEntityDefaultValueValidatorImpl();
    final List<String> caseRoles = new ArrayList<>();
    final EventComplexTypeEntity eventCaseFieldEntity = new EventComplexTypeEntity();
    final List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase = new ArrayList();
    private static final String ROLE1 = "ROLE1";
    private static final String ROLE2 = "ROLE2";
    final String eventId = "eventId";
    private static final String GLOBAL_ROLE_COLLABORATOR = "[COLLABORATOR]";

    final EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext =
        new EventCaseFieldEntityValidationContext(eventId, allEventCaseFieldEntitiesForEventCase, caseRoles);

    @BeforeEach
    void setUp() {
        eventCaseFieldEntity.setReference(ORGANISATION_POLICY_ROLE);
        caseRoles.add(ROLE1);
        caseRoles.add(ROLE2);
    }

    @Test
    void should_pass_validation() {

        eventCaseFieldEntity.setDefaultValue(ROLE2);
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    void should_pass_validation_complex_field_reference() {
        eventCaseFieldEntity.setReference("TestComplexField." + ORGANISATION_POLICY_ROLE);
        eventCaseFieldEntity.setDefaultValue(ROLE2);
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    @Test
    void should_pass_validation_due_to_global_role() {

        eventCaseFieldEntity.setDefaultValue(GLOBAL_ROLE_COLLABORATOR);
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }


    @Test
    void should_not_pass_validation_due_to_incorrect_role() {

        eventCaseFieldEntity.setDefaultValue("xxx");
        final ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity, eventCaseFieldEntityValidationContext);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        assertTrue(validationResult.getValidationErrors().get(0)
            .getDefaultMessage().contains("is not a valid role for"));
    }
}
