package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityCORValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityComplexACLValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidComplexCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidMetadataFieldValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidAccessProfileValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityFieldValueValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityMandatoryFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityUniquenessValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityFieldLabelValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidAccessProfileValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityNonUniqueReferenceValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityReferenceSpellingValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityIncorrectOrderValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityMissingOrderValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldShowConditionReferencesInvalidFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupColumnNumberValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidEventFieldShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidTabFieldShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidTabShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.EventEntityMissingForPageTypeDisplayGroupError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityCaseTypeAccessProfileValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.CreateEventDoesNotHavePostStateValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityCanSaveDraftValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityEnableConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidDefaultPostStateError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidPostStatePriorityError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidAccessProfileValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityShowConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldCaseHistoryViewerCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldDisplayContextValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldLabelCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldMetadataValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldOrderSummaryCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityDefaultValueError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype.EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityACLValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile.UserProfileValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.util.PublishFieldError;
import uk.gov.hmcts.ccd.definition.store.excel.parser.EntityToDefinitionDataItemRegistry;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

@Component
public class SpreadsheetValidationErrorMessageCreator implements ValidationErrorMessageCreator {

    public static final String INVALID_CRUD_VALUE_V_IN_TAB_T_CASE_TYPE_C_CASE_FIELD_F
        = "Invalid CRUD value '%s' in %s tab, case type '%s', case field '%s', ";
    public static final String DEFINED_IN_CASE_ROLES_SHEET_VERIFICATION_MESSAGE
        = "Please make sure it is defined in the CaseRoles sheet.";

    private static final String NOT_DEFINED = "not defined";

    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    public SpreadsheetValidationErrorMessageCreator(EntityToDefinitionDataItemRegistry
                                                        entityToDefinitionDataItemRegistry) {
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    @Override
    public String createErrorMessage(CaseTypeEntityMissingSecurityClassificationValidationError
                                         caseTypeEntityMissingSecurityClassificationValidationError) {
        return createMissingOrInvalidSecurityClassificationMessage(
            caseTypeEntityMissingSecurityClassificationValidationError.getCaseTypeEntity(),
            caseTypeEntityMissingSecurityClassificationValidationError.getCaseTypeEntity().getReference(),
            caseTypeEntityMissingSecurityClassificationValidationError.getDefaultMessage());
    }

    @Override
    public String createErrorMessage(CaseTypeEntityNonUniqueReferenceValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getCaseTypeEntity(),
            def -> String.format(
                "Case Type with name '%s' on tab '%s' already exists. "
                    + "Case types must be unique across all existing jurisdictions.",
                error.getCaseTypeEntity().getReference(),
                def.getSheetName())
        );
    }

    @Override
    public String createErrorMessage(CaseFieldEntityMissingSecurityClassificationValidationError
                                         caseFieldEntityMissingSecurityClassificationValidationError) {
        return createMissingOrInvalidSecurityClassificationMessage(
            caseFieldEntityMissingSecurityClassificationValidationError.getCaseFieldEntity(),
            caseFieldEntityMissingSecurityClassificationValidationError.getCaseFieldEntity().getReference(),
            caseFieldEntityMissingSecurityClassificationValidationError.getDefaultMessage());
    }

    @Override
    public String createErrorMessage(CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                                         validationError) {
        return String.format("%s values cannot have lower security classification than case type; " + "%s entry with "
                + "" + "" + "id '%s' has a security classification of '%s' " + "but %s '%s' has a "
                + "security " + "" + "classification of '%s'",
            CASE_FIELD.getName(),
            CASE_FIELD.getName(),
            validationError.getCaseFieldEntity().getReference(),
            capitalize(validationError.getCaseFieldEntity()
                .getSecurityClassification()
                .toString()
                .toLowerCase()),
            CASE_TYPE.getName(),
            validationError.getCaseFieldEntityValidationContext().getCaseName(),
            capitalize(validationError.getCaseFieldEntityValidationContext()
                .getParentSecurityClassification()
                .toString()
                .toLowerCase()));
    }

    @Override
    public String createErrorMessage(ComplexFieldEntityMissingSecurityClassificationValidationError
                                         complexFieldEntityMissingSecurityClassificationValidationError) {

        return createMissingOrInvalidSecurityClassificationMessage(
            complexFieldEntityMissingSecurityClassificationValidationError.getComplexFieldEntity(),
            complexFieldEntityMissingSecurityClassificationValidationError.getComplexFieldEntity().getReference(),
            complexFieldEntityMissingSecurityClassificationValidationError.getDefaultMessage());

    }

    @Override
    public String createErrorMessage(ComplexFieldEntityMissingOrderValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(ComplexFieldEntityIncorrectOrderValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(
        ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
            complexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError) {
        return String.format("%s values cannot have lower security classification than case field; " + "%s entry "
                + "with" + " id '%s' has a security classification of '%s' " + "but %s entry with id "
                + "'%s' has a" + " security classification of '%s'",
            SheetName.COMPLEX_TYPES.getName(),
            SheetName.COMPLEX_TYPES.getName(),
            complexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                .getComplexFieldEntity()
                .getReference(),
            capitalize(
                complexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                    .getComplexFieldEntity()
                    .getSecurityClassification()
                    .toString()
                    .toLowerCase()),
            CASE_FIELD.getName(),
            complexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                .getComplexFieldEntityValidationContext()
                .getCaseFieldReference(),
            capitalize(
                complexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                    .getComplexFieldEntityValidationContext()
                    .getParentSecurityClassification()
                    .toString()
                    .toLowerCase()));
    }

    @Override
    public String createErrorMessage(EventEntityMissingSecurityClassificationValidationError
                                         eventEntityMissingSecurityClassificationValidationError) {

        return createMissingOrInvalidSecurityClassificationMessage(
            eventEntityMissingSecurityClassificationValidationError.getEventEntity(),
            eventEntityMissingSecurityClassificationValidationError.getEventEntity().getReference(),
            eventEntityMissingSecurityClassificationValidationError.getDefaultMessage());

    }

    @Override
    public String createErrorMessage(EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                                         validationError) {
        return String.format("%s values cannot have lower security classification than case type; " + "%s entry with "
                + "" + "id '%s' has a security classification of '%s' " + "but %s '%s' has a security "
                + "" + "classification of '%s'",
            SheetName.CASE_EVENT.getName(),
            SheetName.CASE_EVENT.getName(),
            validationError.getEventEntity().getReference(),
            capitalize(validationError.getEventEntity()
                .getSecurityClassification()
                .toString()
                .toLowerCase()),
            CASE_TYPE.getName(),
            validationError.getEventEntityValidationContext().getCaseName(),
            capitalize(validationError.getEventEntityValidationContext()
                .getParentSecurityClassification()
                .toString()
                .toLowerCase()));
    }

    @Override
    public String createErrorMessage(final CaseTypeEntityInvalidAccessProfileValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getCaseTypeAccessProfileEntity(),

            def -> CaseRoleServiceImpl.isCaseRole(defaultString(def.getString(ColumnName.ACCESS_PROFILE)))
                ? String.format(
                "Invalid case role '%s' in %s tab for case type '%s'. "
                    + DEFINED_IN_CASE_ROLES_SHEET_VERIFICATION_MESSAGE,
                def.getString(ColumnName.ACCESS_PROFILE),
                def.getSheetName(),
                error.getAuthorisationValidationContext().getCaseReference()) :
                String.format(
                    "Invalid access profile '%s' in %s tab for case type '%s'",
                    def.getString(ColumnName.ACCESS_PROFILE),
                    def.getSheetName(),
                    error.getAuthorisationValidationContext().getCaseReference())
        );
    }

    @Override
    public String createErrorMessage(final CaseTypeEntityInvalidCrudValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getCaseTypeAccessProfileEntity(),
            def -> String.format(
                "Invalid CRUD value '%s' in %s tab for case type '%s', access profile '%s'",
                def.getString(ColumnName.CRUD),
                def.getSheetName(),
                error.getAuthorisationValidationContext().getCaseReference(),
                def.getString(ColumnName.ACCESS_PROFILE)));
    }

    @Override
    public String createErrorMessage(final CaseFieldEntityInvalidCrudValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getCaseFieldACLEntity(),
            def -> String.format(
                INVALID_CRUD_VALUE_V_IN_TAB_T_CASE_TYPE_C_CASE_FIELD_F
                    + "access profile '%s'",
                defaultString(def.getString(ColumnName.CRUD)),
                def.getSheetName(),
                def.getString(ColumnName.CASE_TYPE_ID),
                def.getString(ColumnName.CASE_FIELD_ID),
                defaultString(def.getString(ColumnName.ACCESS_PROFILE))));
    }

    @Override
    public String createErrorMessage(final CaseFieldEntityInvalidComplexCrudValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getComplexFieldACLEntity(),
            def -> String.format(
                INVALID_CRUD_VALUE_V_IN_TAB_T_CASE_TYPE_C_CASE_FIELD_F
                    + "list element code '%s', access profile '%s'",
                defaultString(def.getString(ColumnName.CRUD)),
                def.getSheetName(),
                def.getString(ColumnName.CASE_TYPE_ID),
                def.getString(ColumnName.CASE_FIELD_ID),
                def.getString(ColumnName.LIST_ELEMENT_CODE),
                defaultString(def.getString(ColumnName.ACCESS_PROFILE))));
    }

    @Override
    public String createErrorMessage(final CaseFieldEntityComplexACLValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getComplexFieldACLEntity(),
            def -> String.format(
                INVALID_CRUD_VALUE_V_IN_TAB_T_CASE_TYPE_C_CASE_FIELD_F
                    + "list element code '%s', access profile '%s'. Detail: %s",
                defaultString(def.getString(ColumnName.CRUD)),
                def.getSheetName(),
                def.getString(ColumnName.CASE_TYPE_ID),
                def.getString(ColumnName.CASE_FIELD_ID),
                def.getString(ColumnName.LIST_ELEMENT_CODE),
                defaultString(def.getString(ColumnName.ACCESS_PROFILE)),
                error.getDefaultMessage()));
    }

    @Override
    public String createErrorMessage(final CaseFieldEntityInvalidAccessProfileValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getCaseFieldACLEntity(),
            def -> CaseRoleServiceImpl.isCaseRole(defaultString(def.getString(ColumnName.ACCESS_PROFILE)))
                ? String.format(
                "Invalid case role '%s' in %s tab, case type '%s', case field '%s'. "
                    + DEFINED_IN_CASE_ROLES_SHEET_VERIFICATION_MESSAGE,
                defaultString(def.getString(ColumnName.ACCESS_PROFILE)),
                def.getSheetName(),
                def.getString(ColumnName.CASE_TYPE_ID),
                def.getString(ColumnName.CASE_FIELD_ID)) :
                String.format(
                    "Invalid access profile '%s' in %s tab, case type '%s', case field '%s', crud '%s'",
                    defaultString(def.getString(ColumnName.ACCESS_PROFILE)),
                    def.getSheetName(),
                    def.getString(ColumnName.CASE_TYPE_ID),
                    def.getString(ColumnName.CASE_FIELD_ID),
                    defaultString(def.getString(ColumnName.CRUD))));
    }

    @Override
    public String createErrorMessage(final EventEntityInvalidCrudValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getEventACLEntity(),
            def -> String.format(
                "Invalid CRUD value '%s' in %s tab, case type '%s', event '%s', access profile '%s'",
                defaultString(def.getString(ColumnName.CRUD)),
                def.getSheetName(),
                error.getAuthorisationEventValidationContext().getCaseReference(),
                error.getAuthorisationEventValidationContext().getEventReference(),
                defaultString(def.getString(ColumnName.ACCESS_PROFILE))));
    }

    @Override
    public String createErrorMessage(final EventEntityInvalidAccessProfileValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getEventACLEntity(),
            def -> CaseRoleServiceImpl.isCaseRole(defaultString(def.getString(ColumnName.ACCESS_PROFILE)))
                ? String.format(
                "Invalid case role '%s' in %s tab, case type '%s', event '%s'. "
                    + DEFINED_IN_CASE_ROLES_SHEET_VERIFICATION_MESSAGE,
                defaultString(def.getString(ColumnName.ACCESS_PROFILE)),
                def.getSheetName(),
                error.getAuthorisationEventValidationContext().getCaseReference(),
                error.getAuthorisationEventValidationContext().getEventReference()) :
                String.format(
                    "Invalid access profile '%s' in %s tab, case type '%s', event '%s', crud '%s'",
                    defaultString(def.getString(ColumnName.ACCESS_PROFILE)),
                    def.getSheetName(),
                    error.getAuthorisationEventValidationContext().getCaseReference(),
                    error.getAuthorisationEventValidationContext().getEventReference(),
                    defaultString(def.getString(ColumnName.CRUD))));
    }

    @Override
    public String createErrorMessage(CreateEventDoesNotHavePostStateValidationError error) {
        return newMessageIfDefinitionExists(error, error.getEventEntity(), def -> {
            String postConditionValue = def.getString(ColumnName.POST_CONDITION_STATE);
            return String.format("Event '%s' is invalid create event as Postcondition is %s in %s tab",
                error.getEventEntity().getReference(),
                StringUtils.isEmpty(postConditionValue) ? NOT_DEFINED : postConditionValue,
                def.getSheetName());
        });
    }

    @Override
    public String createErrorMessage(EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError error) {
        return newMessageIfDefinitionExists(error,
            error.getEventCaseFieldEntity(),
            def -> String.format(
                "Unknown field '%s' for event '%s' in show condition: '%s' on tab '%s'",
                error.getShowConditionField(),
                error.getEventId(),
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                def.getSheetName()));

    }

    @Override
    public String createErrorMessage(EventCaseFieldEntityInvalidShowConditionError error) {
        return newMessageIfDefinitionExists(error,
            error.getEventCaseFieldEntity(),
            def -> String.format("Invalid show condition '%s' for event '%s' on tab '%s'",
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                error.getValidationContext().getEventId(),
                def.getSheetName()));
    }

    @Override
    public String createErrorMessage(EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError error) {
        return newMessageIfDefinitionExists(error,
            error.getEventComplexTypeEntity(),
            def -> String.format("Unknown field '%s' for event '%s' and element '%s' in show "
                    + "condition: '%s' on tab '%s'",
                error.getShowConditionField(),
                error.getEventId(),
                error.getEventComplexTypeEntity().getReference(),
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                def.getSheetName()));
    }

    @Override
    public String createErrorMessage(EventComplexTypeEntityInvalidShowConditionError error) {
        return newMessageIfDefinitionExists(error,
            error.getEventCaseFieldEntity(),
            def -> String.format("Invalid show condition '%s' for event '%s' on tab '%s'",
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                error.getValidationContext().getEventId(),
                def.getSheetName()));
    }

    @Override
    public String createErrorMessage(EventComplexTypeEntityDefaultValueError error) {
        return error.toString();
    }

    @Override
    public String createErrorMessage(PublishFieldError error) {
        return error.toString();
    }

    @Override
    public String createErrorMessage(CaseFieldEntityInvalidMetadataFieldValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getMetadataField(), def -> String.format("%s. WorkSheet '%s'", error.getDefaultMessage(),
                def.getSheetName()));
    }

    @Override
    public String createErrorMessage(EventEntityMissingForPageTypeDisplayGroupError eventMissingError) {
        return String.format("Event is missing for displayGroup '%s' and with label '%s'",
            eventMissingError.getCaseTypeAccessProfileEntity().getReference(),
            eventMissingError.getCaseTypeAccessProfileEntity().getLabel());
    }

    @Override
    public String createErrorMessage(DisplayGroupInvalidShowConditionError error) {
        return newMessageIfDefinitionExists(error,
            error.getDisplayGroupEntity(),
            def -> String.format("Invalid show condition '%s' for display group '%s' on tab '%s'",
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                error.getDisplayGroupEntity().getReference(),
                def.getSheetName()));
    }

    @Override
    public String createErrorMessage(DisplayGroupInvalidEventFieldShowCondition error) {
        return newMessageIfDefinitionExists(error,
            error.getDisplayGroup(),
            def -> String.format("Invalid show condition '%s' for display group '%s' on tab '%s': "
                    + "unknown field '%s' for event '%s'",
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                error.getDisplayGroup().getReference(),
                def.getSheetName(),
                error.getShowConditionField(),
                error.getDisplayGroup().getEvent().getReference()));
    }

    @Override
    public String createErrorMessage(DisplayGroupInvalidTabShowCondition error) {
        return newMessageIfDefinitionExists(error,
            error.getDisplayGroup(),
            error.getShowConditionField() != null
                ? def -> String.format("Invalid show condition '%s' for tab '%s' on spreadsheet tab '%s': "
                    + "unknown field '%s'",
                def.getString(ColumnName.TAB_SHOW_CONDITION),
                error.getDisplayGroup().getReference(),
                def.getSheetName(),
                error.getShowConditionField()) : def -> error.getDefaultMessage());
    }

    @Override
    public String createErrorMessage(DisplayGroupInvalidTabFieldShowCondition error) {
        return newMessageIfDefinitionExists(error,
            error.getDisplayGroupCaseField(),
            error.getShowConditionField() != null
                ? def -> String.format("Invalid show condition '%s' for tab field '%s' on spreadsheet tab '%s': "
                    + "unknown field '%s'",
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                error.getDisplayGroupCaseField().getCaseField().getReference(),
                def.getSheetName(),
                error.getShowConditionField()) : def -> error.getDefaultMessage()
        );
    }

    @Override
    public String createErrorMessage(ComplexFieldInvalidShowConditionError error) {
        return newMessageIfDefinitionExists(error,
            error.getComplexField(),
            def -> String.format("Invalid show condition '%s' for complex field element '%s' on tab " + "'%s'",
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                error.getComplexField().getReference(),
                def.getSheetName()));
    }

    @Override
    public String createErrorMessage(ComplexFieldShowConditionReferencesInvalidFieldError error) {
        return newMessageIfDefinitionExists(error,
            error.getComplexField(),
            def -> String.format(
                "Unknown field '%s' of complex field '%s' in show condition: '%s' on " + "" + "tab '%s'",
                error.getShowConditionField(),
                error.getComplexField().getComplexFieldType().getReference(),
                def.getString(ColumnName.FIELD_SHOW_CONDITION),
                def.getSheetName()));
    }


    @Override
    public String createErrorMessage(EventCaseFieldDisplayContextValidatorImpl.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(
        EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(
        EventCaseFieldCaseHistoryViewerCaseFieldValidator.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(EventCaseFieldOrderSummaryCaseFieldValidator.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(EventCaseFieldLabelCaseFieldValidator.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(EventEntityCanSaveDraftValidatorImpl.ValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(CaseRoleEntityFieldValueValidatorImpl.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(CaseRoleEntityUniquenessValidatorImpl.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(DisplayGroupColumnNumberValidator.ValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(GenericLayoutEntityValidatorImpl.ValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(StateEntityACLValidatorImpl.ValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(StateEntityCrudValidatorImpl.ValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(EventCaseFieldMetadataValidatorImpl.ValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(UserProfileValidatorImpl.ValidationError validationError) {
        return withWorkSheetName(validationError);
    }

    @Override
    public String createErrorMessage(CaseTypeEntityReferenceSpellingValidationError error) {
        return newMessageIfDefinitionExists(error,
            error.getCaseTypeEntity(),
            def -> String.format(
                "Case Type with ID '%s' on tab '%s' already exists with the current spelling '%s'. %s",
                error.getCaseTypeEntity().getReference(),
                def.getSheetName(),
                error.getDefinitiveCaseTypeReference(),
                "This spelling must be used for the Case Type ID.")
        );
    }

    @Override
    public String createErrorMessage(
        CaseTypeEntityFieldLabelValidator.PlaceholderLeafNotSimpleTypeValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(
        CaseTypeEntityFieldLabelValidator.PlaceholderCannotBeResolvedValidationError error) {
        return withWorkSheetName(error);
    }

    @Override
    public String createErrorMessage(EventEntityInvalidPostStatePriorityError error) {
        return newMessageIfDefinitionExists(error, error.getEventEntity(), def -> {
            String postConditionValue = def.getString(ColumnName.POST_CONDITION_STATE);
            return String.format("Post state condition %s has duplicate priorities for event '%s' in %s tab",
                StringUtils.isEmpty(postConditionValue) ? NOT_DEFINED : postConditionValue,
                error.getEventEntity().getReference(),
                def.getSheetName());
        });
    }

    @Override
    public String createErrorMessage(EventEntityInvalidDefaultPostStateError error) {
        return newMessageIfDefinitionExists(error, error.getEventEntity(), def -> {
            String postConditionValue = def.getString(ColumnName.POST_CONDITION_STATE);
            return String.format("Post state condition %s has to include "
                    + "non conditional post state for event '%s' in %s tab",
                StringUtils.isEmpty(postConditionValue) ? NOT_DEFINED : postConditionValue,
                error.getEventEntity().getReference(),
                def.getSheetName());
        });
    }

    @Override
    public String createErrorMessage(EventEntityShowConditionReferencesInvalidCaseFieldError error) {
        return newMessageIfDefinitionExists(error, error.getEventEntity(), def -> {
            String postConditionValue = def.getString(ColumnName.POST_CONDITION_STATE);
            return formattedErrorMessage("Unknown field '%s' for event '%s' in post state condition: '%s' in %s tab",
                error.getShowConditionField(),
                error.getEventEntity().getReference(),
                postConditionValue,
                def.getSheetName());
        });
    }

    @Override
    public String createErrorMessage(EventEntityEnableConditionReferencesInvalidCaseFieldError error) {
        return newMessageIfDefinitionExists(error, error.getEventEntity(), def -> {
            String condition = def.getString(ColumnName.EVENT_ENABLING_CONDITION);
            return formattedErrorMessage("Unknown field '%s' for event '%s'"
                    + " in event enabling condition: '%s' in %s tab",
                error.getShowConditionField(),
                error.getEventEntity().getReference(),
                condition,
                def.getSheetName());
        });
    }

    @Override
    public String createErrorMessage(EventEntityCaseTypeAccessProfileValidationError
                                         eventEntityCaseTypeAccessProfileValidationError) {
        return newMessageIfDefinitionExists(eventEntityCaseTypeAccessProfileValidationError,
            eventEntityCaseTypeAccessProfileValidationError.getEventACLEntity(),
            def -> String.format("%s in worksheet '%s'",
                    eventEntityCaseTypeAccessProfileValidationError.getDefaultMessage(),
                    def.getSheetName()));
    }

    @Override
    public String createErrorMessage(CaseFieldEntityCORValidationError error) {
        return newMessageIfDefinitionExists(error, error.getCaseFieldEntity(),
            def -> String.format("%s in worksheet '%s'", error.getDefaultMessage(), def.getSheetName()));
    }

    private String formattedErrorMessage(String message,
                                         String showConditionField,
                                         String eventReference,
                                         String condition,
                                         String sheetName) {
        return String.format(message,
            showConditionField,
            eventReference,
            StringUtils.isEmpty(condition) ? NOT_DEFINED : condition,
            sheetName);
    }

    private String withWorkSheetName(SimpleValidationError<?> error) {
        return newMessageIfDefinitionExists(error,
            error.getEntity(),
            def -> String.format("%s. WorkSheet '%s'", error.getDefaultMessage(), def.getSheetName()));
    }

    private String newMessageIfDefinitionExists(ValidationError error,
                                                Object entity,
                                                Function<DefinitionDataItem, String> newMessage) {
        Optional<DefinitionDataItem> definitionDataItem = entityToDefinitionDataItemRegistry.getForEntity(entity);
        return definitionDataItem.map(newMessage::apply).orElse(error.getDefaultMessage());
    }

    private String createMissingOrInvalidSecurityClassificationMessage(Object entity,
                                                                       String entryId,
                                                                       String defaultMessage) {

        Optional<DefinitionDataItem> definitionDataItem = entityToDefinitionDataItemRegistry.getForEntity(entity);

        return definitionDataItem.map(def -> {
            String securityClassificationInSpreadsheet = def.getString(ColumnName.SECURITY_CLASSIFICATION);

            if (StringUtils.isEmpty(StringUtils.trimAllWhitespace(securityClassificationInSpreadsheet))) {
                return String.format("SecurityClassification is not defined for entry with id '%s' in '%s'",
                    entryId,
                    def.getSheetName());
            } else {
                return String.format("Invalid security classification definition '%s' for entry with id '%s' in '%s'",
                    securityClassificationInSpreadsheet,
                    entryId,
                    def.getSheetName());
            }
        }).orElse(defaultMessage);
    }
}
