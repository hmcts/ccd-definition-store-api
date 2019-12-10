package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationEventValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityComplexACLValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidComplexCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidMetadataFieldValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityFieldValueValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityMandatoryFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityUniquenessValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityFieldLabelValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityNonUniqueReferenceValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityReferenceSpellingValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.CaseFieldComplexFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldShowConditionReferencesInvalidFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupColumnNumberValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidEventFieldShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidTabFieldShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidTabShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.CreateEventDoesNotHavePostStateValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityCanSaveDraftValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldCaseHistoryViewerCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldDisplayContextValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldLabelCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldMetadataValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldOrderSummaryCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityACLValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile.UserProfileValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.excel.parser.EntityToDefinitionDataItemRegistry;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    public void testCaseTypeEntityNonUniqueReferenceValidationError_defaultMessageReturned() {

        CaseTypeEntity caseTypeEntity = caseTypeEntity("Case Type Name");
        CaseTypeEntityNonUniqueReferenceValidationError caseTypeEntityNonUniqueReferenceValidationError
            = new CaseTypeEntityNonUniqueReferenceValidationError(caseTypeEntity);
        assertEquals(
            caseTypeEntityNonUniqueReferenceValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                caseTypeEntityNonUniqueReferenceValidationError
            )
        );
    }

    @Test
    public void testCaseTypeEntityNonUniqueReferenceValidationError_customMessageReturned() {

        CaseTypeEntity caseTypeEntity = caseTypeEntity("Case Type Name");
        assertCaseTypeEntityNonUniqueReferenceValidationErrorForEntityFromDataDefinitionItem(
            "Case Type with name 'Case Type Name' on tab 'CaseType' already exists. "
                + "Case types must be unique across all existing jurisdictions.",
            caseTypeEntity,
            definitionDataItem(SheetName.CASE_TYPE, ColumnName.CASE_TYPE_ID, "Other Case Type Reference"));

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

        assertEquals("CaseField values cannot have lower security classification than case type; "
                + "CaseField entry with id 'Case Field Reference' has a security classification of 'Public' "
                + "but CaseType 'Case Name' has a security classification of 'Private'",
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

        assertEquals("ComplexTypes values cannot have lower security classification than case field; "
                + "ComplexTypes entry with id 'Complex Field Reference' has a security classification of 'Public' "
                + "but CaseField entry with id 'Case Field Reference' has a security classification of 'Private'",
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

        assertEquals("CaseEvent values cannot have lower security classification than case type; "
                + "CaseEvent entry with id 'Event Reference' has a security classification of 'Public' "
                + "but CaseType 'Case Name' has a security classification of 'Private'",
            classUnderTest.createErrorMessage(
                new EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    eventEntity("Event Reference", SecurityClassification.PUBLIC),
                    eventEntityValidationContext("Case Name", SecurityClassification.PRIVATE)
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
    public void caseTypeEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned_whenIdamRoleMissing() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseTypeACLEntity caseTypeACLEntity = caseTypeUserRoleEntity("crud");

        final CaseTypeEntityInvalidUserRoleValidationError error = new CaseTypeEntityInvalidUserRoleValidationError(
            caseTypeACLEntity, new AuthorisationValidationContext(caseTypeEntity));

        when(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_TYPE,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "user role"))
            );

        assertEquals(
            "Invalid idam role 'user role' in AuthorisationCaseType tab for case type 'case type'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseTypeEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned_whenCaseRoleMissing() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseTypeACLEntity caseTypeACLEntity = caseTypeUserRoleEntity("crud");

        final CaseTypeEntityInvalidUserRoleValidationError error = new CaseTypeEntityInvalidUserRoleValidationError(
            caseTypeACLEntity, new AuthorisationValidationContext(caseTypeEntity));

        when(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_TYPE,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "[CASE_ROLE]"))
            );

        assertEquals(
            "Invalid case role '[CASE_ROLE]' in AuthorisationCaseType tab for case type 'case type'." +
                " Please make sure it is defined in the CaseRoles sheet.",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseTypeUserRoleEntityInvalidCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseTypeACLEntity caseTypeACLEntity = caseTypeUserRoleEntity("Xcrud");

        final CaseTypeEntityInvalidCrudValidationError error = new CaseTypeEntityInvalidCrudValidationError(
            caseTypeACLEntity, new AuthorisationValidationContext(caseTypeEntity));

        when(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity))
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
        final CaseTypeACLEntity caseTypeACLEntity = caseTypeUserRoleEntity("Xcrud");

        final CaseTypeEntityInvalidCrudValidationError error = new CaseTypeEntityInvalidCrudValidationError(
            caseTypeACLEntity, new AuthorisationValidationContext(caseTypeEntity));

        assertEquals(
            "Invalid CRUD value 'Xcrud' for case type 'case type'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned_whenIdamRoleMissing() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldACLEntity entity = caseFieldUserRoleEntity("crud");

        final CaseFieldEntityInvalidUserRoleValidationError error = new CaseFieldEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity,
                new CaseFieldEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "X"),
                    new ImmutablePair<>(ColumnName.CRUD, "Y"))
            );

        assertEquals(
            "Invalid idam role 'X' in AuthorisationCaseField tab, case type 'case type', case field 'case field', crud 'Y'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned_whenCaseRoleMissing() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldACLEntity entity = caseFieldUserRoleEntity("crud");

        final CaseFieldEntityInvalidUserRoleValidationError error = new CaseFieldEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity,
                new CaseFieldEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "[SOME_CASE_ROLE]"))
            );

        assertEquals(
            "Invalid case role '[SOME_CASE_ROLE]' in AuthorisationCaseField tab, case type 'case type', case field 'case field'." +
                " Please make sure it is defined in the CaseRoles sheet.",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidUserRole_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldACLEntity entity = caseFieldUserRoleEntity("crud");

        final CaseFieldEntityInvalidUserRoleValidationError error = new CaseFieldEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity,
                new CaseFieldEntityValidationContext(caseTypeEntity)));

        assertEquals(
            "Invalid UserRole for case type 'case type', case field 'case field'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final CaseFieldACLEntity entity = caseFieldUserRoleEntity("Xcrud");

        final CaseFieldEntityInvalidCrudValidationError error = new CaseFieldEntityInvalidCrudValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity,
                new CaseFieldEntityValidationContext(caseTypeEntity)));

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
        final CaseFieldACLEntity entity = caseFieldUserRoleEntity("Xcrud");

        final CaseFieldEntityInvalidCrudValidationError error = new CaseFieldEntityInvalidCrudValidationError(entity,
            new AuthorisationCaseFieldValidationContext(
                caseFieldEntity,
                new CaseFieldEntityValidationContext(
                    caseTypeEntity)));

        assertEquals("Invalid CRUD value 'Xcrud' for case type 'case type', case field 'case field'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidComplexCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final ComplexFieldACLEntity entity = complexFieldACLEntity("Xcrud", "code1");

        final CaseFieldEntityInvalidComplexCrudValidationError error = new CaseFieldEntityInvalidComplexCrudValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity,
                new CaseFieldEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.LIST_ELEMENT_CODE, "list element code"),
                    new ImmutablePair<>(ColumnName.CRUD, "Xcrud"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "user role"))
            );

        assertEquals(
            "Invalid CRUD value 'Xcrud' in AuthorisationCaseField tab, case type 'case type', case field 'case field', list element code 'list element code', user role 'user role'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void caseFieldUserRoleEntityInvalidComplexCrud_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final ComplexFieldACLEntity entity = complexFieldACLEntity("Xcrud", "code1");

        final CaseFieldEntityInvalidComplexCrudValidationError error = new CaseFieldEntityInvalidComplexCrudValidationError(entity,
            new AuthorisationCaseFieldValidationContext(
                caseFieldEntity,
                new CaseFieldEntityValidationContext(
                    caseTypeEntity)));

        assertEquals("Invalid CRUD value 'Xcrud' for case type 'case type', case field 'case field', list element code 'code1'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void complexACLHasMoreAccessThanParentValidationError_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final ComplexFieldACLEntity entity = complexFieldACLEntity("Xcrud", "code1");

        final CaseFieldEntityComplexACLValidationError error = new CaseFieldEntityComplexACLValidationError(
            entity,
            new AuthorisationCaseFieldValidationContext(caseFieldEntity,
                new CaseFieldEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_FIELD,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_FIELD_ID, "case field"),
                    new ImmutablePair<>(ColumnName.LIST_ELEMENT_CODE, "list element code"),
                    new ImmutablePair<>(ColumnName.CRUD, "Xcrud"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "user role"))
            );

        assertEquals(
            "Invalid CRUD value 'Xcrud' in AuthorisationCaseField tab, case type 'case type', case field "
                + "'case field', list element code 'list element code', user role 'user role'. Detail: The access for "
                + "case type 'case type', case field 'case field', list element code 'code1' is more than its parent",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void complexACLHasMoreAccessThanParentValidationError_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);
        final ComplexFieldACLEntity entity = complexFieldACLEntity("Xcrud", "code1");

        final CaseFieldEntityComplexACLValidationError error = new CaseFieldEntityComplexACLValidationError(entity,
            new AuthorisationCaseFieldValidationContext(
                caseFieldEntity,
                new CaseFieldEntityValidationContext(
                    caseTypeEntity)));

        assertEquals("The access for case type 'case type', case field 'case field', list element code 'code1' is more than its parent",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void shouldHaveValidationMessageForCaseFieldEntityInvalidMetadataFieldValidationError() {
        CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        CaseFieldEntity caseFieldEntity = caseFieldEntity("case field", SecurityClassification.RESTRICTED);

        CaseFieldEntityInvalidMetadataFieldValidationError error = new CaseFieldEntityInvalidMetadataFieldValidationError(
            "Invalid metadata field 'case field' declaration for case type 'case type'",
            caseFieldEntity,
            new CaseFieldEntityValidationContext(caseTypeEntity));

        assertEquals("Invalid metadata field 'case field' declaration for case type 'case type'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned_whenIdamRoleMissing() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventACLEntity entity = eventUserRoleEntity(null, "crud");

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
            "Invalid idam role 'u' in AuthorisationCaseEvent tab, case type 'case type', event 'event', crud 'x'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventEntityInvalidUserRole_createErrorMessageCalled_customMessageReturned_whenCaseRoleMissing() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventACLEntity entity = eventUserRoleEntity(null, "crud");

        final EventEntityInvalidUserRoleValidationError error = new EventEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationEventValidationContext(eventEntity, new EventEntityValidationContext(caseTypeEntity)));

        when(entityToDefinitionDataItemRegistry.getForEntity(entity))
            .thenReturn(
                definitionDataItem(SheetName.AUTHORISATION_CASE_EVENT,
                    new ImmutablePair<>(ColumnName.CASE_TYPE_ID, "case type"),
                    new ImmutablePair<>(ColumnName.CASE_EVENT_ID, "event"),
                    new ImmutablePair<>(ColumnName.CRUD, "x"),
                    new ImmutablePair<>(ColumnName.USER_ROLE, "[CASE_ROLE]"))
            );

        assertEquals(
            "Invalid case role '[CASE_ROLE]' in AuthorisationCaseEvent tab, case type 'case type', event 'event'." +
                " Please make sure it is defined in the CaseRoles sheet.",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventUserRoleEntityInvalidUserRole_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventACLEntity entity = eventUserRoleEntity(null, "crud");

        final EventEntityInvalidUserRoleValidationError error = new EventEntityInvalidUserRoleValidationError(
            entity,
            new AuthorisationEventValidationContext(eventEntity, new EventEntityValidationContext(caseTypeEntity)));

        assertEquals(
            "Invalid UserRole for case type 'case type', event 'event'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void eventUserRoleEntityInvalidCrud_createErrorMessageCalled_customMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventACLEntity entity = eventUserRoleEntity("user role", "Xcrud");

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
    public void eventUserRoleEntityInvalidCrud_createErrorMessageCalled_defaultMessageReturned() {
        final CaseTypeEntity caseTypeEntity = caseTypeEntity("case type");
        final EventEntity eventEntity = eventEntity("event", SecurityClassification.RESTRICTED);
        final EventACLEntity entity = eventUserRoleEntity("user role", "Xcrud");

        final EventEntityInvalidCrudValidationError error = new EventEntityInvalidCrudValidationError(entity,
            new AuthorisationEventValidationContext(
                eventEntity,
                new EventEntityValidationContext(
                    caseTypeEntity)));

        assertEquals("Invalid CRUD value 'Xcrud' for case type 'case type', event 'event'",
            classUnderTest.createErrorMessage(error));
    }

    @Test
    public void entityExistsInRegistry_testCreateErrorMessageEventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError_customMessageReturned() {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();

        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("SHOW CONDITION");

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventCaseFieldEntity))).thenReturn(
            Optional.of(definitionDataItem));

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
    public void entityDoesNotExistInRegistry_testCreateErrorMessageEventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError_defaultMsgReturned() {
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
    public void entityExistsInRegistry_EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError() {
        EventCaseFieldEntityValidationContext context = mock(EventCaseFieldEntityValidationContext.class);
        given(context.getEventId()).willReturn("EVENT ID");
        EventComplexTypeEntity eventComplexTypeEntity = mock(EventComplexTypeEntity.class);
        given(eventComplexTypeEntity.getReference()).willReturn("REFERENCE");

        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("SHOW CONDITION");

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventComplexTypeEntity)))
            .thenReturn(Optional.of(definitionDataItem));

        assertEquals(
            "Unknown field 'SHOW CONDITION FIELD' for event 'EVENT ID' and element 'REFERENCE' in show condition: 'SHOW CONDITION' on tab 'EventToComplexTypes'",
            classUnderTest.createErrorMessage(
                new EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError("SHOW CONDITION FIELD",
                    context,
                    eventComplexTypeEntity
                )));
    }

    @Test
    public void entityDoesNotExistInRegistry_EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError_defaultMessage() {
        EventCaseFieldEntityValidationContext context = mock(EventCaseFieldEntityValidationContext.class);
        EventComplexTypeEntity eventComplexTypeEntity = new EventComplexTypeEntity();

        EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError error =
            new EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError("", context,
                eventComplexTypeEntity);

        assertEquals(error.getDefaultMessage(), classUnderTest.createErrorMessage(error));
    }

    @Test
    public void entityExistsInRegistry_EventComplexTypeEntityInvalidShowConditionError() {
        EventComplexTypeEntity eventComplexTypeEntity = mock(EventComplexTypeEntity.class);
        EventCaseFieldEntityValidationContext context = mock(EventCaseFieldEntityValidationContext.class);
        given(context.getEventId()).willReturn("EVENT ID");

        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_COMPLEX_TYPES.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("SHOW CONDITION");

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventComplexTypeEntity)))
            .thenReturn(Optional.of(definitionDataItem));

        assertEquals(
            "Invalid show condition 'SHOW CONDITION' for event 'EVENT ID' on tab 'EventToComplexTypes'",
            classUnderTest.createErrorMessage(
                new EventComplexTypeEntityInvalidShowConditionError(eventComplexTypeEntity, context)));
    }

    @Test
    public void entityDoesNotExistInRegistry_EventComplexTypeEntityInvalidShowConditionError() {
        EventComplexTypeEntity eventComplexTypeEntity = mock(EventComplexTypeEntity.class);
        EventCaseFieldEntityValidationContext context = mock(EventCaseFieldEntityValidationContext.class);

        EventComplexTypeEntityInvalidShowConditionError error =
            new EventComplexTypeEntityInvalidShowConditionError(eventComplexTypeEntity, context);

        assertEquals(error.getDefaultMessage(), classUnderTest.createErrorMessage(error));
    }

    @Test
    public void entityExistsInRegistry_testCreateErrorMessageEventCaseFieldEntityShowConditionInvalidError_customMessageReturned() {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setShowCondition("SHOW CONDITION ON ENTITY");

        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn(
            "SHOW CONDITION FROM SPREADSHEET");

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventCaseFieldEntity))).thenReturn(
            Optional.of(definitionDataItem));

        assertEquals(
            "Invalid show condition 'SHOW CONDITION FROM SPREADSHEET' for event 'EVENT ID' on tab 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldEntityInvalidShowConditionError(
                    eventCaseFieldEntity,
                    eventCaseFieldEntityValidationContext("EVENT ID"))));
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
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(displayGroupEntity))).thenReturn(
            Optional.of(definitionDataItem));

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
    public void entityExistsInRegistry_DisplayGroupInvalidEventShowConditionField_customMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("dg");
        displayGroupEntity.setShowCondition("sc");
        EventEntity event = new EventEntity();
        event.setReference("event");
        displayGroupEntity.setEvent(event);
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(displayGroupEntity))).thenReturn(
            Optional.of(definitionDataItem));

        assertEquals(
            "Invalid show condition 'sc' for display group 'dg' on tab 'CaseEventToFields': unknown field 'field' for event 'event'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidEventFieldShowCondition("field", displayGroupEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidEventShowConditionField_defaultMessageReturned() {

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
                new DisplayGroupInvalidEventFieldShowCondition("field", displayGroupEntity))
        );
    }

    @Test
    public void entityExistsInRegistry_DisplayGroupInvalidTabShowConditionField_customMessageReturned() {

        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setCaseField(caseFieldEntity("dg", SecurityClassification.PUBLIC));
        displayGroupCaseFieldEntity.setShowCondition("sc");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_TYPE_TAB.toString());
        when(definitionDataItem.getString(eq(ColumnName.FIELD_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(displayGroupCaseFieldEntity))).thenReturn(
            Optional.of(definitionDataItem));

        assertEquals(
            "Invalid show condition 'sc' for tab field 'dg' on spreadsheet tab 'CaseTypeTab': unknown field 'field'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidTabFieldShowCondition("field", displayGroupCaseFieldEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidTabShowConditionField_defaultMessageReturned() {

        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setCaseField(caseFieldEntity("dg", SecurityClassification.PUBLIC));
        displayGroupCaseFieldEntity.setShowCondition("sc");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_TYPE_TAB.toString());

        assertEquals(
            "Invalid show condition 'sc' for tab field 'dg': unknown field 'field'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidTabFieldShowCondition("field", displayGroupCaseFieldEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidTabShowConditionField_defaultMessageWhenNoShowCondition() {

        DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity = new DisplayGroupCaseFieldEntity();
        displayGroupCaseFieldEntity.setCaseField(caseFieldEntity("dg", SecurityClassification.PUBLIC));

        assertEquals(
            "Invalid show condition 'null' for tab field 'dg': unknown field 'field'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidTabFieldShowCondition("field", displayGroupCaseFieldEntity))
        );
    }

    @Test
    public void entityExistsInRegistry_DisplayGroupInvalidTabShowCondition_customMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setShowCondition("sc");
        displayGroupEntity.setReference("dg");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_TYPE_TAB.toString());
        when(definitionDataItem.getString(eq(ColumnName.TAB_SHOW_CONDITION))).thenReturn("sc");
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(displayGroupEntity))).thenReturn(
            Optional.of(definitionDataItem));

        assertEquals(
            "Invalid show condition 'sc' for tab 'dg' on spreadsheet tab 'CaseTypeTab': unknown field 'field'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidTabShowCondition("field", displayGroupEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidTabShowCondition_defaultMessageReturned() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setShowCondition("sc");
        displayGroupEntity.setReference("dg");
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_TYPE_TAB.toString());

        assertEquals(
            "Invalid show condition 'sc' for tab 'dg': unknown field 'field'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidTabShowCondition("field", displayGroupEntity))
        );
    }

    @Test
    public void entityNotInRegistry_DisplayGroupInvalidTabShowCondition_defaultMessageWhenNoShowCondition() {

        DisplayGroupEntity displayGroupEntity = new DisplayGroupEntity();
        displayGroupEntity.setReference("dg");

        assertEquals(
            "Invalid show condition 'null' for tab 'dg': unknown field 'field'",
            classUnderTest.createErrorMessage(
                new DisplayGroupInvalidTabShowCondition("field", displayGroupEntity))
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
                new DisplayGroupColumnNumberValidator.ValidationError("default message",
                    new DisplayGroupCaseFieldEntity()))
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
                new GenericLayoutEntityValidatorImpl.ValidationError("default message",
                    new SearchInputCaseFieldEntity()))
        );
    }

    @Test
    public void testCreateErrorMessage_StateUserRoleEntityValidationError_customMessageReturned() {

        StateACLEntity entity = new StateACLEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.AUTHORISATION_CASE_STATE.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'AuthorisationCaseState'",
            classUnderTest.createErrorMessage(
                new StateEntityACLValidatorImpl.ValidationError("default message", entity))
        );
    }

    @Test
    public void testCreateErrorMessage_StateUserRoleEntityValidationError_defaultMessageReturned() {
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new StateEntityACLValidatorImpl.ValidationError("default message", new StateACLEntity()))
        );
    }

    @Test
    public void testCreateErrorMessage_StateEntityCrudValidatorImplValidationError_customMessageReturned() {

        StateACLEntity entity = new StateACLEntity();
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
                new StateEntityCrudValidatorImpl.ValidationError("default message", new StateACLEntity()))
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

    @Test
    public void shouldHaveValidValidationMessageForOrderSummaryValidationError() {

        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldOrderSummaryCaseFieldValidator.ValidationError("default message", entity))
        );
    }

    @Test
    public void shouldHaveValidValidationMessageForLabelValidationError() {

        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldLabelCaseFieldValidator.ValidationError("default message", entity))
        );
    }

    @Test
    public void shouldHaveValidValidationMessageForCasePaymentHistoryViewerValidationError() {

        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator.ValidationError("default message",
                    entity))
        );
    }

    @Test
    public void shouldHaveValidValidationMessageForCaseHistoryViewerValidationError() {

        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldCaseHistoryViewerCaseFieldValidator.ValidationError("default message",
                    entity))
        );
    }

    @Test
    public void shouldHaveValidationMessageForEventCaseFieldMetadataValidationError() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT_TO_FIELDS.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(entity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("default message. WorkSheet 'CaseEventToFields'",
            classUnderTest.createErrorMessage(
                new EventCaseFieldMetadataValidatorImpl.ValidationError("default message", entity))
        );
    }

    @Test
    public void shouldHaveValidationMessageForUserProfileValidationError() {
        assertEquals("Invalid CaseType in workbasket user default; user: 'null', jurisdiction: 'null', "
            + "case type: 'null', state: 'null'",
            classUnderTest.createErrorMessage(
                new UserProfileValidatorImpl.ValidationError("CaseType", new WorkBasketUserDefault()))
        );
    }

    @Test
    public void shouldHaveValidationMessageForPlaceholderLeafNotASimpleTypeValidationError() {
        assertEquals("Label of caseField 'FieldId' has placeholder 'OtherFieldId.CollectionId.ComplexId' "
                         + "that points to case field 'ComplexId' of non simple type",
            classUnderTest.createErrorMessage(
                new CaseTypeEntityFieldLabelValidator.PlaceholderLeafNotSimpleTypeValidationError("FieldId",
                                                                                                  "OtherFieldId.CollectionId.ComplexId",
                                                                                                  "ComplexId",
                                                                                                  new CaseFieldEntity()))
        );
    }

    @Test
    public void shouldHaveValidationMessageForPlaceholderCannotBeResolvedValidationError() {
        assertEquals("Label of caseField 'FieldId' has placeholder 'OtherFieldId.CollectionId.ComplexId' that points to unknown case field",
            classUnderTest.createErrorMessage(
                new CaseTypeEntityFieldLabelValidator.PlaceholderCannotBeResolvedValidationError("FieldId",
                                                                                                  "OtherFieldId.CollectionId.ComplexId",
                                                                                                  new CaseFieldEntity()))
        );
    }

    @Test
    public void testCreateErrorMessage_EventEntityCanSaveDraftValidatorImplValidationError_customMessageReturned() {

        EventEntity eventEntity = new EventEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_EVENT.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(eventEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("Custom message. WorkSheet 'CaseEvent'",
            classUnderTest.createErrorMessage(
                new EventEntityCanSaveDraftValidatorImpl.ValidationError("Custom message", eventEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_EventEntityCanSaveDraftValidatorImplValidationError_defaultMessageReturned() {
        EventEntity eventEntity = new EventEntity();
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new EventEntityCanSaveDraftValidatorImpl.ValidationError("default message", eventEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_CaseRoleEntityMandatoryFieldsValidatorImplValidationError_customMessageReturned() {

        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_ROLE.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseRoleEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("Custom message. WorkSheet 'CaseRoles'",
            classUnderTest.createErrorMessage(
                new CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError("Custom message", caseRoleEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_CaseRoleEntityMandatoryFieldsValidatorImplValidationError_defaultMessageReturned() {
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError("default message", caseRoleEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_CaseRoleEntityFieldValueValidatorImplValidationError_customMessageReturned() {

        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_ROLE.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseRoleEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("Custom message. WorkSheet 'CaseRoles'",
            classUnderTest.createErrorMessage(
                new CaseRoleEntityFieldValueValidatorImpl.ValidationError("Custom message", caseRoleEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_CaseRoleEntityFieldValueValidatorImplValidationError_defaultMessageReturned() {
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new CaseRoleEntityFieldValueValidatorImpl.ValidationError("default message", caseRoleEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_CaseRoleEntityUniquenessValidatorImplValidationError_customMessageReturned() {

        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        DefinitionDataItem definitionDataItem = mock(DefinitionDataItem.class);
        when(definitionDataItem.getSheetName()).thenReturn(SheetName.CASE_ROLE.toString());
        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseRoleEntity))).thenReturn(Optional.of(definitionDataItem));

        assertEquals("Custom message. WorkSheet 'CaseRoles'",
            classUnderTest.createErrorMessage(
                new CaseRoleEntityUniquenessValidatorImpl.ValidationError("Custom message", caseRoleEntity))
        );
    }

    @Test
    public void testCreateErrorMessage_CaseRoleEntityUniquenessValidatorImplValidationError_defaultMessageReturned() {
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        assertEquals(
            "default message",
            classUnderTest.createErrorMessage(
                new CaseRoleEntityUniquenessValidatorImpl.ValidationError("default message", caseRoleEntity))
        );
    }

    @Test
    public void testCaseTypeEntityReferenceSpellingValidationError_defaultMessageReturned() {

        CaseTypeEntity caseTypeEntity = caseTypeEntity("Case Type Name");
        CaseTypeEntityReferenceSpellingValidationError caseTypeEntityReferenceSpellingValidationError
            = new CaseTypeEntityReferenceSpellingValidationError("Definitive Case Type ID", caseTypeEntity);
        assertEquals(
            caseTypeEntityReferenceSpellingValidationError.getDefaultMessage(),
            classUnderTest.createErrorMessage(
                caseTypeEntityReferenceSpellingValidationError
            )
        );
    }

    @Test
    public void testCaseTypeEntityReferenceSpellingValidationError_customMessageReturned() {

        CaseTypeEntity caseTypeEntity = caseTypeEntity("Case Type Reference");
        assertCaseTypeEntityReferenceSpellingValidationErrorForEntityFromDataDefinitionItem(
            "Case Type with ID 'Case Type Reference' on tab 'CaseType' already exists with the current spelling "
                + "'Definitive Case Type Reference'. This spelling must be used for the Case Type ID.",
            "Definitive Case Type Reference",
            caseTypeEntity,
            definitionDataItem(SheetName.CASE_TYPE, ColumnName.CASE_TYPE_ID, "Other Case Type Reference"));

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
        CaseFieldEntityValidationContext caseFieldEntityValidationContext = mock(
            CaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getCaseName()).thenReturn(caseName);
        when(caseFieldEntityValidationContext.getParentSecurityClassification()).thenReturn(
            parentSecurityClassification);
        return caseFieldEntityValidationContext;
    }

    private ComplexFieldEntity complexFieldEntity(String reference,
                                                  SecurityClassification securityClassification) {

        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setSecurityClassification(securityClassification);
        return complexFieldEntity;

    }

    private CaseFieldComplexFieldEntityValidator.ValidationContext complexFieldEntityValidationContext(
        String caseFieldReference,
        SecurityClassification parentSecurityClassification) {

        CaseFieldComplexFieldEntityValidator.ValidationContext complexFieldEntityValidationContext = mock(
            CaseFieldComplexFieldEntityValidator.ValidationContext.class);
        when(complexFieldEntityValidationContext.getCaseFieldReference()).thenReturn(caseFieldReference);
        when(complexFieldEntityValidationContext.getParentSecurityClassification()).thenReturn(
            parentSecurityClassification);
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

    private CaseTypeACLEntity caseTypeUserRoleEntity(final String crud) {
        final CaseTypeACLEntity entity = new CaseTypeACLEntity();
        setAuthorisation(entity, crud);
        return entity;
    }

    private CaseFieldACLEntity caseFieldUserRoleEntity(final String crud) {
        final CaseFieldACLEntity entity = new CaseFieldACLEntity();
        setAuthorisation(entity, crud);
        return entity;
    }

    private ComplexFieldACLEntity complexFieldACLEntity(final String crud, final String code) {
        final ComplexFieldACLEntity entity = new ComplexFieldACLEntity();
        setAuthorisation(entity, crud);
        entity.setListElementCode(code);
        return entity;
    }

    private EventACLEntity eventUserRoleEntity(final String role, final String crud) {
        final EventACLEntity entity = new EventACLEntity();
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

        EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext = mock(
            EventCaseFieldEntityValidationContext.class);
        when(eventCaseFieldEntityValidationContext.getEventId()).thenReturn(eventId);
        return eventCaseFieldEntityValidationContext;

    }

    private Optional<DefinitionDataItem> definitionDataItem(SheetName sheetName, ColumnName columnName,
                                                            String columnValue) {
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

    private void assertCreateEventDoesNotHavePostStateValidationErrorMessageForEntityFromDataDefinitionItem(
        String message,
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

    private void assertCaseTypeEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
        String message,
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

    private void assertCaseTypeEntityNonUniqueReferenceValidationErrorForEntityFromDataDefinitionItem(
        String message,
        CaseTypeEntity caseTypeEntity,
        Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseTypeEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new CaseTypeEntityNonUniqueReferenceValidationError(caseTypeEntity)
            )
        );

    }

    private void assertCaseFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
        String message,
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

    private void assertEventEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
        String message,
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

    private void assertComplexFieldEntityMissingSecurityClassificationValidationErrorForEntityFromDataDefinitionItem(
        String message,
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

    private void assertCaseTypeEntityReferenceSpellingValidationErrorForEntityFromDataDefinitionItem(
        String message,
        String definitiveCaseTypeReference,
        CaseTypeEntity caseTypeEntity,
        Optional<DefinitionDataItem> definitionDataItem) {

        when(entityToDefinitionDataItemRegistry.getForEntity(eq(caseTypeEntity))).thenReturn(definitionDataItem);

        assertEquals(
            message,
            classUnderTest.createErrorMessage(
                new CaseTypeEntityReferenceSpellingValidationError(definitiveCaseTypeReference, caseTypeEntity)
            )
        );

    }
}
