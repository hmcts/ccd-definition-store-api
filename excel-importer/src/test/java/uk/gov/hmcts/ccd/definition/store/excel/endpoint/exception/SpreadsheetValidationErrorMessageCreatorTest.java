package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype
    .CaseTypeEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupColumnNumberValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidShowConditionFieldForEvent;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityUserRoleValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.excel.parser.EntityToDefinitionDataItemRegistry;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class SpreadsheetValidationErrorMessageCreatorTest {

    @Mock
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @InjectMocks
    private SpreadsheetValidationErrorMessageCreator classUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(entityToDefinitionDataItemRegistry.getForEntity(anyObject())).thenReturn(Optional.empty());
    }

    @Test
    public void entityExistsInRegistry_testCaseTypeEntityMissingSecurityClassificationValidationError_customMessageReturned() {

        CaseTypeEntity caseTypeEntity = caseTypeEntity("Case Type Reference");

        assertCaseTypeEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Case Type Reference' in 'CaseType'",
            caseTypeEntity,
            definitionDataItem(SheetName.CASE_TYPE, ColumnName.SECURITY_CLASSIFICATION, null)
        );

        assertCaseTypeEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Case Type Reference' in 'CaseType'",
            caseTypeEntity,
            definitionDataItem(SheetName.CASE_TYPE, ColumnName.SECURITY_CLASSIFICATION, "    ")
        );

        assertCaseTypeEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "Invalid security classification definition 'XXXX' for entry with id 'Case Type Reference' in 'CaseType'",
            caseTypeEntity,
            definitionDataItem(SheetName.CASE_TYPE, ColumnName.SECURITY_CLASSIFICATION, "XXXX")
        );

    }

    @Test
    public void entityDoesNotExistInRegistry_testCaseTypeEntityMissingSecurityClassificationValidationError_defaultMessageReturned() {

        CaseTypeEntity caseTypeEntity = caseTypeEntity("Case Name");
        CaseTypeEntityMissingSecurityClassificationValidationError caseTypeEntityMissingSecurityClassificationValidationError
            = new CaseTypeEntityMissingSecurityClassificationValidationError(caseTypeEntity);
        assertEquals(
            caseTypeEntityMissingSecurityClassificationValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                caseTypeEntityMissingSecurityClassificationValidationError
            )
        );

    }

    @Test
    public void entityExistsInRegistry_testCaseFieldEntityMissingSecurityClassificationValidationError_customMessageReturned() {

        CaseFieldEntity caseFieldEntity = caseFieldEntity("Case Field Reference", null);

        assertCaseFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Case Field Reference' in 'CaseField'",
            caseFieldEntity,
            definitionDataItem(SheetName.CASE_FIELD, ColumnName.SECURITY_CLASSIFICATION, null)
        );

        assertCaseFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Case Field Reference' in 'CaseField'",
            caseFieldEntity,
            definitionDataItem(SheetName.CASE_FIELD, ColumnName.SECURITY_CLASSIFICATION, "    ")
        );

        assertCaseFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "Invalid security classification definition 'XXXX' for entry with id 'Case Field Reference' in 'CaseField'",
            caseFieldEntity,
            definitionDataItem(SheetName.CASE_FIELD, ColumnName.SECURITY_CLASSIFICATION, "XXXX")
        );

    }

    @Test
    public void entityDoesNotExistInRegistry_testCaseFieldEntityMissingSecurityClassificationValidationError_defaultMessageReturned() {

        CaseFieldEntity caseFieldEntity = caseFieldEntity("Case Field Reference", null);
        CaseFieldEntityMissingSecurityClassificationValidationError caseFieldEntityMissingSecurityClassificationValidationError
            = new CaseFieldEntityMissingSecurityClassificationValidationError(caseFieldEntity, null);
        assertEquals(
            caseFieldEntityMissingSecurityClassificationValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                caseFieldEntityMissingSecurityClassificationValidationError
            )
        );

    }

    @Test
    public void testCreateErrorMessage_CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError() {

        assertEquals("CaseField values cannot have lower security classification than case type; " +
                "CaseField entry with id 'Case Field Reference' has a security classification of 'Public' " +
                "but CaseType 'Case Name' has a security classification of 'Private'",
            classUnderTest.createErrorMessage(
                new CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    caseFieldEntity("Case Field Reference", SecurityClassification.PUBLIC),
                    caseFieldEntityValidationContext("Case Name", SecurityClassification.PRIVATE)
                )
            )
        );

    }

    @Test
    public void entityExistsInRegistry_testComplexFieldEntityMissingSecurityClassificationValidationError_customMessageReturned() {

        ComplexFieldEntity complexFieldEntity = complexFieldEntity("Complex Field Reference", null);

        assertComplexFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Complex Field Reference' in 'ComplexTypes'",
            complexFieldEntity,
            definitionDataItem(SheetName.COMPLEX_TYPES, ColumnName.SECURITY_CLASSIFICATION, null)
        );

        assertComplexFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Complex Field Reference' in 'ComplexTypes'",
            complexFieldEntity,
            definitionDataItem(SheetName.COMPLEX_TYPES, ColumnName.SECURITY_CLASSIFICATION, "    ")
        );

        assertComplexFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "Invalid security classification definition 'XXXX' for entry with id 'Complex Field Reference' in 'ComplexTypes'",
            complexFieldEntity,
            definitionDataItem(SheetName.COMPLEX_TYPES, ColumnName.SECURITY_CLASSIFICATION, "XXXX")
        );

    }

    @Test
    public void entityDoesNotExistInRegistry_testComplexFieldEntityMissingSecurityClassificationValidationError_defaultMessageReturned() {

        ComplexFieldEntity complexFieldEntity = complexFieldEntity("Complex Field Reference", null);
        ComplexFieldEntityMissingSecurityClassificationValidationError complexFieldEntityMissingSecurityClassificationValidationError
            = new ComplexFieldEntityMissingSecurityClassificationValidationError(complexFieldEntity);
        assertEquals(
            complexFieldEntityMissingSecurityClassificationValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                complexFieldEntityMissingSecurityClassificationValidationError
            )
        );

    }

    @Test
    public void testCreateErrorMessage_ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError() {

        assertEquals("ComplexTypes values cannot have lower security classification than case field; " +
                "ComplexTypes entry with id 'Complex Field Reference' has a security classification of 'Public' " +
                "but CaseField entry with id 'Case Field Reference' has a security classification of 'Private'",
            classUnderTest.createErrorMessage(
                new ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    complexFieldEntity("Complex Field Reference", SecurityClassification.PUBLIC),
                    complexFieldEntityValidationContext("Case Field Reference", SecurityClassification.PRIVATE)
                )
            )
        );

    }

    @Test
    public void entityExistsInRegistry_testCreateErrorMessageForEventEntityMissingSecurityClassificationValidationError_customMessageReturned() {

        EventEntity eventEntity = eventEntity("Event Reference", null);

        assertEventEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Event Reference' in 'CaseEvent'",
            eventEntity,
            definitionDataItem(SheetName.CASE_EVENT, ColumnName.SECURITY_CLASSIFICATION, null)
        );

        assertEventEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "SecurityClassification is not defined for entry with id 'Event Reference' in 'CaseEvent'",
            eventEntity,
            definitionDataItem(SheetName.CASE_EVENT, ColumnName.SECURITY_CLASSIFICATION, "    ")
        );

        assertEventEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
            "Invalid security classification definition 'XXXX' for entry with id 'Event Reference' in 'CaseEvent'",
            eventEntity,
            definitionDataItem(SheetName.CASE_EVENT, ColumnName.SECURITY_CLASSIFICATION, "XXXX")
        );

    }

    @Test
    public void entityDoesNotExistInRegistry_testCreateErrorMessageForEventEntityMissingSecurityClassificationValidationError_defaultMessageReturned() {

        EventEntity eventEntity = eventEntity("Event Reference", null);
        EventEntityMissingSecurityClassificationValidationError eventEntityMissingSecurityClassificationValidationError
            = new EventEntityMissingSecurityClassificationValidationError(eventEntity);
        assertEquals(
            eventEntityMissingSecurityClassificationValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                eventEntityMissingSecurityClassificationValidationError
            )
        );

    }

    @Test
    public void testCreateErrorMessage_EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError() {

        assertEquals("CaseEvent values cannot have lower security classification than case type; " +
                "CaseEvent entry with id 'Event Reference' has a security classification of 'Public' " +
                "but CaseType 'Case Name' has a security classification of 'Private'",
            classUnderTest.createErrorMessage(
                new EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    eventEntity("Event Reference", SecurityClassification.PUBLIC),
                    eventEntityValidationContext("Case Name", SecurityClassification.PRIVATE)
                )
            )
        );

    }

    @Test
    public void testCreateErrorMessage_LabelTypeCannotBeEditableValidationError() {

        assertEquals("Case Field Reference is Label type and cannot be editable for the Event Reference in the tab CaseEventToFields",
            classUnderTest.createErrorMessage(
                new LabelTypeCannotBeEditableValidationError(
                    eventCaseFieldEntity(
                        eventEntity("Event Reference", null),
                        caseFieldEntity("Case Field Reference", null)
                    )
                )
            )
        );

    }

    @Test
    public void entityExistsInRegistry_createErrorMessageCalledForCreateEventDoesNotHavePostStateValidationError_customMessageReturned() {

        EventEntity eventEntity = eventEntity("Event ID", null);

        assertCreateEventDoesNotHavePostStateValidationErrorMessageForEntityFromDataDefinitionItem(
            "Event 'Event ID' is invalid create event as Postcondition is * in CaseEvent tab",
            eventEntity,
            definitionDataItem(SheetName.CASE_EVENT, ColumnName.POST_CONDITION_STATE, "*")
        );

        assertCreateEventDoesNotHavePostStateValidationErrorMessageForEntityFromDataDefinitionItem(
            "Event 'Event ID' is invalid create event as Postcondition is not defined in CaseEvent tab",
            eventEntity,
            definitionDataItem(SheetName.CASE_EVENT, ColumnName.POST_CONDITION_STATE, null)
        );

        assertCreateEventDoesNotHavePostStateValidationErrorMessageForEntityFromDataDefinitionItem(
            "Event 'Event ID' is invalid create event as Postcondition is not defined in CaseEvent tab",
            eventEntity,
            definitionDataItem(SheetName.CASE_EVENT, ColumnName.POST_CONDITION_STATE, "  ")
        );

    }

    @Test
    public void entityDoesNotExistInRegistry_createErrorMessageCalled_defaultMessageReturned() {

        EventEntity eventEntity = eventEntity("Event ID", null);

        CreateEventDoesNotHavePostStateValidationError createEventDoesNotHavePostStateValidationError
            = new CreateEventDoesNotHavePostStateValidationError(
            eventEntity
        );

        assertEquals(
            createEventDoesNotHavePostStateValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(createEventDoesNotHavePostStateValidationError)
        );

    }

    @Test
    public void caseTypeUserRoleEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = caseTypeUserRoleEntity("crud");

        final CaseTypeEntityInvalidUserRoleValidationError error = new CaseTypeEntityInvalidUserRoleValidationError(
            caseTypeUserRoleEntity, new AuthorisationValidationContext(caseTypeEntity));

        assertEquals(
            "Invalid UserRole is not defined for case type 'case type'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseTypeUserRoleEntityInvalidCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = caseTypeUserRoleEntity("Xcrud");

        final CaseTypeEntityInvalidCrudValidationError error = new CaseTypeEntityInvalidCrudValidationError(
            caseTypeUserRoleEntity, new AuthorisationValidationContext(caseTypeEntity));

        when(entityToDefinitionDataItemRegistry.getForEntity(caseTypeUserRoleEntity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_TYPE,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "user role"),
                    new ImmutablePair<>(ColumnName.CRUD, "Xcrud"))
            );

        assertEquals(
            "Invalid CRUD value 'Xcrud' in AuthorisationCaseType tab for case type 'case type', user role 'user role'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseTypeUserRoleEnttyInvalidCrud_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = caseTypeUserRoleEntity("Xcrud");

        final CaseTypeEntityInvalidCrudValidationError error = new CaseTypeEntityInvalidCrudValidationError(
            caseTypeUserRoleEntity, new AuthorisationValidationContext(caseTypeEntity));

        assertEquals(
            "Invalid CRUD value 'Xcrud' for case type 'case type'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldUserRoleEntity entity = caseFieldUserRoleEntity("crud");

        final CaseFieldEntityInvalidUserRoleValidationError error = new CaseFieldEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity, new CaseFieldEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "X"),
                    new ImmutablePair<>(ColumnName.CRUD, "Y"))
            );

        assertEquals(
            "Invalid IdamRole 'X' in AuthorisationCaseField tab, case type 'case type', case field 'case field', crud 'Y'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidUserRole_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldUserRoleEntity entity = caseFieldUserRoleEntity("crud");

        final CaseFieldEntityInvalidUserRoleValidationError error = new CaseFieldEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity, new CaseFieldEntityValidationContext(caseTypeEntity)));

        assertEquals(
            "Invalid UserRole for case type 'case type', case field 'case field'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEnttyInvalidCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldUserRoleEntity entity = caseFieldUserRoleEntity("Xcrud");

        final CaseFieldEntityInvalidCrudValidationError error = new CaseFieldEntityInvalidCrudValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity, new CaseFieldEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.CRUD, "Xcrud"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "user role"))
            );

        assertEquals(
            "Invalid CRUD value 'Xcrud' in AuthorisationCaseField tab, case type 'case type', case field 'case field', user role 'user role'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidCrud_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldUserRoleEntity entity = caseFieldUserRoleEntity("Xcrud");

        final CaseFieldEntityInvalidCrudValidationError error = new CaseFieldEntityInvalidCrudValidationError(entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity, new CaseFieldEntityValidationContext(caseTypeEntity)));

        assertEquals("Invalid CRUD value 'Xcrud' for case type 'case type', case field 'case field'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventFieldUserRoleEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventUserRoleEntity entity = eventUserRoleEntity(null, "crud");

        final EventEntityInvalidUserRoleValidationError error = new EventEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationEventValidationContext(eventEntity, new EventEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_EVENT,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_EVENT_ID, "event"),
                    new ImmutablePair<>(ColumnName.CRUD, "x"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "u"))
            );

        assertEquals(
            "Invalid IdamRole 'u' in AuthorisationCaseEvent tab, case type 'case type', event 'event', crud 'x'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventUserRoleEntityInvalidUserRole_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventUserRoleEntity entity = eventUserRoleEntity(null, "crud");

        final EventEntityInvalidUserRoleValidationError error = new EventEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationEventValidationContext(eventEntity, new EventEntityValidationContext(caseTypeEntity)));

        assertEquals(
            "Invalid UserRole for case type 'case type', event 'event'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventUserRoleEnttyInvalidCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventUserRoleEntity entity = eventUserRoleEntity("user role", "Xcrud");

        final EventEntityInvalidCrudValidationError error = new EventEntityInvalidCrudValidationError(
            entity,
            new AuthorisationEventValidationContext(eventEntity, new EventEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.CRUD, "Xcrud"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "user role"))
            );

        assertEquals(
            "Invalid CRUD value 'Xcrud' in AuthorisationCaseField tab, case type 'case type', event 'event', user role 'user role'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventUserRoleEnttyInvalidCrud_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventUserRoleEntity entity = eventUserRoleEntity("user role", "Xcrud");

        final EventEntityInvalidCrudValidationError error = new EventEntityInvalidCrudValidationError(entity, new AuthorisationEventValidationContext(eventEntity, new EventEntityValidationContext(caseTypeEntity)));

        assertEquals("Invalid CRUD value 'Xcrud' for case type 'case type', event 'event'", classUnderTest.createErrorMessage(error));
    }

    @Test
    public void entityExistsInRegistry_testCreateErrorMessageEventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError_customMessageReturned() {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();

        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("SHOW CONDITION");

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventCaseFieldEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
            "Unknown field 'SHOW CONDITION FIELD' for event 'EVENT ID' in show condition: 'SHOW CONDITION' on tab 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
                    "SHOW CONDITION FIELD",
                    eventCaseFieldEntityValidationContext("EVENT ID"),
                    eventCaseFieldEntity
                )
            )
        );
    }

    @Test
    public void entityDoesNotExistInRegistry_testCreateErrorMessageEventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError_defaultMessageReturned() {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();

        EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError
            = new EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
                "SHOW CONDITION FIELD",
                eventCaseFieldEntityValidationContext("EVENT ID"),
                eventCaseFieldEntity
            );

        assertEquals(
            eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError
            )
        );

    }

    @Test
    public void entityExistsInRegistry_testCreateErrorMessageEventCaseFieldEntityShowConditionInvalidError_customMessageReturned() {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setShowCondition("SHOW CONDITION ON ENTITY");

        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("SHOW CONDITION FROM SPREADSHEET");

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventCaseFieldEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
            "Invalid show condition 'SHOW CONDITION FROM SPREADSHEET' for event 'EVENT ID' on tab 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldEntityInvalidShowConditionError(
                    eventCaseFieldEntity,
                    eventCaseFieldEntityValidationContext("EVENT ID")
                )
            )
        );
    }

    @Test
    public void entityDoesNotExistInRegistry_testCreateErrorMessageEventCaseFieldEntityShowConditionInvalidError_defaultMessageReturned() {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();

        EventCaseFieldEntityInvalidShowConditionError eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError
            = new EventCaseFieldEntityInvalidShowConditionError(
            eventCaseFieldEntity,
            eventCaseFieldEntityValidationContext("EVENT ID")
        );

        assertEquals(
            eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError
            )
        );
    }

    @Test
    public void entityExistsInRegistry_DisplayGroupInvalidShowConditionError_customMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("dg");
        displayGroupEntity.setShowCondition("sc");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(displayGroupEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
                "Invalid show condition 'sc' for display group 'dg' on tab 'CaseEventToFields'",
                classUnderTest.createErrorMessage(
                        new DisplayGroupInvalidShowConditionError(displayGroupEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidShowConditionError_defaultMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("dg");
        displayGroupEntity.setShowCondition("sc");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());

        assertEquals(
                "Invalid show condition 'sc' for display group 'dg'",
                classUnderTest.createErrorMessage(
                        new DisplayGroupInvalidShowConditionError(displayGroupEntity))
        );
    }

    @Test
    public void entityExistsInRegistry_DisplayGroupInvalidShowConditionField_customMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("dg");
        displayGroupEntity.setShowCondition("sc");
        EventEntity event = new EventEntity();
        event.setReference("event");
        displayGroupEntity.setEvent(event);
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(displayGroupEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
                "Invalid show condition 'sc' for display group 'dg' on tab 'CaseEventToFields': unknown field 'field' for event 'event'",
                classUnderTest.createErrorMessage(
                        new DisplayGroupInvalidShowConditionFieldForEvent("field", displayGroupEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidShowConditionField_defaultMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("dg");
        displayGroupEntity.setShowCondition("sc");
        EventEntity event = new EventEntity();
        event.setReference("event");
        displayGroupEntity.setEvent(event);
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());

        assertEquals(
                "Invalid show condition 'sc' for display group 'dg': unknown field 'field' for event 'event'",
                classUnderTest.createErrorMessage(
                        new DisplayGroupInvalidShowConditionFieldForEvent("field", displayGroupEntity))
        );
    }

    @Test
    public void entityExistsInRegistry_ComplexFieldInvalidShowConditionError_customMessageReturned() {

        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference("f");
        field.setShowCondition("sc");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.COMPLEX_TYPES.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(field))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
                "Invalid show condition 'sc' for complex field element 'f' on tab 'ComplexTypes'",
                classUnderTest.createErrorMessage(
                        new ComplexFieldInvalidShowConditionError(field))
        );
    }

    @Test
    public void entityNotInRegistry_ComplexFieldInvalidShowConditionError_defaultMessageReturned() {

        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference("f");
        field.setShowCondition("sc");

        assertEquals(
                "Show condition 'sc' invalid for complex field element 'f'",
                classUnderTest.createErrorMessage(
                        new ComplexFieldInvalidShowConditionError(field))
        );
    }

    @Test
    public void entityExistsInRegistry_ComplexFieldShowConditionField_customMessageReturned() {

        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference("f");
        field.setShowCondition("sc");
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("complexField");
        field.setComplexFieldType(fieldTypeEntity);
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.COMPLEX_TYPES.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(field))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
                "Unknown field 'field' of complex field 'complexField' in show condition: 'sc' on tab 'ComplexTypes'",
                classUnderTest.createErrorMessage(
                        new ComplexFieldShowConditionReferencesInvalidFieldError("field", field))
        );
    }

    @Test
    public void entityNotInRegistry_ComplexFieldShowConditionField_defaultMessageReturned() {

        ComplexFieldEntity field = new ComplexFieldEntity();
        field.setReference("f");
        field.setShowCondition("sc");
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("complexField");
        field.setComplexFieldType(fieldTypeEntity);
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.COMPLEX_TYPES.toString());

        assertEquals(
                "Unknown field 'field' of complex field 'complexField' in show condition: 'sc'",
                classUnderTest.createErrorMessage(
                        new ComplexFieldShowConditionReferencesInvalidFieldError("field", field))
        );
    }

    @Test
    public void entityExistsInRegistry_DisplayGroupColumnNrValidatorError_customMessageReturned() {

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
                "default message. WorkSheet 'CaseEventToFields'",
                classUnderTest.createErrorMessage(
                        new DisplayGroupColumnNumberValidator.ValidationError("default message", entity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupColumnNrValidatorError_defaultMessageReturned() {
        assertEquals(
                "default message",
                classUnderTest.createErrorMessage(
                        new DisplayGroupColumnNumberValidator.ValidationError("default message", new DisplayGroupCaseFieldEntity()))
        );
    }

    @Test
    public void shouldReturnCustomErrorMessageForGenericLayoutEntityValidatorValidationErrorWhenMessageOverridden() {
        SearchInputCaseFieldEntity entity = new SearchInputCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.SEARCH_INPUT_FIELD.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals(
            "default message. WorkSheet 'SearchInputFields'",
            classUnderTest.createErrorMessage(
                new GenericLayoutEntityValidatorImpl.ValidationError("default message", entity))
        );
    }

    @Test
    public void shouldReturnCustomErrorMessageForGenericLayoutEntityValidatorValidationErrorWhenMessageIsNotOverridden() {
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new GenericLayoutEntityValidatorImpl.ValidationError("default message", new SearchInputCaseFieldEntity()))
        );
    }

    @Test
    public void testCreateErrorMessage_StateUserRoleEntityValidationError_customMessageReturned() {

        StateUserRoleEntity entity = new StateUserRoleEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.AUTHORISATION_CASE_STATE.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'AuthorisationCaseState'",
            classUnderTest.createErrorMessage(
                new StateEntityUserRoleValidatorImpl.ValidationError("default message", entity))
        );
    }

    @Test
    public void testCreateErrorMessage_StateUserRoleEntityValidationError_defaultMessageReturned() {
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new StateEntityUserRoleValidatorImpl.ValidationError("default message", new StateUserRoleEntity()))
        );
    }

    @Test
    public void testCreateErrorMessage_StateEntityCrudValidatorImplValidationError_customMessageReturned() {

        StateUserRoleEntity entity = new StateUserRoleEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.AUTHORISATION_CASE_STATE.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'AuthorisationCaseState'",
            classUnderTest.createErrorMessage(
                new StateEntityCrudValidatorImpl.ValidationError("default message", entity))
        );
    }

    @Test
    public void testCreateErrorMessage_StateEntityCrudValidatorImplValidationError_defaultMessageReturned() {
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new StateEntityCrudValidatorImpl.ValidationError("default message", new StateUserRoleEntity()))
        );
    }


    @Test
    public void testCreateErrorMessage_CaseFieldEntityDisplayContextMustHaveValidValueValidationError() {

        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldDisplayContextValidatorImpl.ValidationError("default message", entity))
        );
    }

    private CaseTypeEntity caseTypeEntity(String reference) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(reference);
        return caseTypeEntity;
    }

    private CaseFieldEntity caseFieldEntity(String reference,
                                            SecurityClassification securityClassification) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setSecurityClassification(securityClassification);
        return caseFieldEntity;
    }

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext(String caseName,
                                                                              SecurityClassification parentSecurityClassification) {
        CaseFieldEntityValidationContext caseFieldEntityValidationContext = mock(CaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getCaseName()).thenReturn(caseName);
        when(caseFieldEntityValidationContext.getParentSecurityClassification()).thenReturn(parentSecurityClassification);
        return caseFieldEntityValidationContext;
    }

    private ComplexFieldEntity complexFieldEntity(String reference,
                                                  SecurityClassification securityClassification) {

        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setSecurityClassification(securityClassification);
        return complexFieldEntity;

    }

    private CaseFieldComplexFieldEntityValidator.ValidationContext complexFieldEntityValidationContext(String caseFieldReference,
                                                                                             SecurityClassification parentSecurityClassification) {

        CaseFieldComplexFieldEntityValidator.ValidationContext complexFieldEntityValidationContext = mock(CaseFieldComplexFieldEntityValidator.ValidationContext.class);
        when(complexFieldEntityValidationContext.getCaseFieldReference()).thenReturn(caseFieldReference);
        when(complexFieldEntityValidationContext.getParentSecurityClassification()).thenReturn(parentSecurityClassification);
        return complexFieldEntityValidationContext;

    }


    private EventEntity eventEntity(String reference, SecurityClassification securityClassification) {

        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference(reference);
        eventEntity.setSecurityClassification(securityClassification);
        return eventEntity;

    }

    private EventCaseFieldEntity eventCaseFieldEntity(EventEntity eventEntity, CaseFieldEntity caseFieldEntity) {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setEvent(eventEntity);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        return eventCaseFieldEntity;

    }

    private CaseTypeUserRoleEntity caseTypeUserRoleEntity(final String crud) {
        final CaseTypeUserRoleEntity entity = new CaseTypeUserRoleEntity();
        setAuthorisation(entity, crud);
        return entity;
    }

    private CaseFieldUserRoleEntity caseFieldUserRoleEntity(final String crud) {
        final CaseFieldUserRoleEntity entity = new CaseFieldUserRoleEntity();
        setAuthorisation(entity, crud);
        return entity;
    }

    private EventUserRoleEntity eventUserRoleEntity(final String role, final String crud) {
        final EventUserRoleEntity entity = new EventUserRoleEntity();
        setAuthorisation(entity, crud);
        return entity;
    }

    private void setAuthorisation(Authorisation entity, final String crud) {
        entity.setCrudAsString(crud);
    }

    private EventEntityValidationContext eventEntityValidationContext(String caseName,
                                                                      SecurityClassification parentSecurityClassification) {

        EventEntityValidationContext eventEntityValidationContext = mock(EventEntityValidationContext.class);
        when(eventEntityValidationContext.getCaseName()).thenReturn(caseName);
        when(eventEntityValidationContext.getParentSecurityClassification()).thenReturn(parentSecurityClassification);
        return eventEntityValidationContext;

    }

    private EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext(String eventId) {

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = mock(EventCaseFieldEntityValidationContext.class);
        when(eventCaseFieldEntityValidationContext.getEventId()).thenReturn(eventId);
        return eventCaseFieldEntityValidationContext;

    }

    private Optional<DefinitionDataItem> definitionDataItem(SheetName sheetName, ColumnName columnName, String columnValue) {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(sheetName.toString());
        definitionDataItem.addAttribute(columnName.toString(), columnValue);
        return Optional.of(definitionDataItem);
    }

    private Optional<DefinitionDataItem> definitionDataItem(SheetName sheetName, Pair<ColumnName, Object>... data) {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(sheetName.toString());

        for (Pair<ColumnName, Object> datum : data) {
            definitionDataItem.addAttribute(datum.getKey().toString(), datum.getValue());
        }

        return Optional.of(definitionDataItem);
    }

    private void assertCreateEventDoesNotHavePostStateValidationErrorMessageForEntityFromDataDefinitionItem(String message,
                                                                                                            EventEntity eventEntity,
                                                                                                            Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new CreateEventDoesNotHavePostStateValidationError(
                    eventEntity
                )
            )
        );

    }

    private void assertCaseTypeEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(String message,
                                                                                                                 CaseTypeEntity caseTypeEntity,
                                                                                                                 Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseTypeEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new CaseTypeEntityMissingSecurityClassificationValidationError(
                    caseTypeEntity
                )
            )
        );

    }

    private void assertCaseFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(String message,
                                                                                                                  CaseFieldEntity caseFieldEntity,
                                                                                                                  Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseFieldEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new CaseFieldEntityMissingSecurityClassificationValidationError(
                    caseFieldEntity, null
                )
            )
        );

    }

    private void assertEventEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(String message,
                                                                                                              EventEntity eventEntity,
                                                                                                              Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new EventEntityMissingSecurityClassificationValidationError(
                    eventEntity
                )
            )
        );

    }

    private void assertComplexFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(String message,
                                                                                                                     ComplexFieldEntity complexFieldEntity,
                                                                                                                     Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(complexFieldEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new ComplexFieldEntityMissingSecurityClassificationValidationError(
                    complexFieldEntity
                )
            )
        );

    }


}
