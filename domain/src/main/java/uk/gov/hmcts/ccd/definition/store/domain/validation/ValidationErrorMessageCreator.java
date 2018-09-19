package uk.gov.hmcts.ccd.definition.store.domain.validation;

import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidMetadataFieldValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityFieldValueValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityMandatoryFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityUniquenessValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldShowConditionReferencesInvalidFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupColumnNumberValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidEventFieldShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidTabFieldShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.DisplayGroupInvalidTabShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup.EventEntityMissingForPageTypeDisplayGroupError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.CreateEventDoesNotHavePostStateValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityCanSaveDraftValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidCrudValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityInvalidUserRoleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityMissingSecurityClassificationValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldDisplayContextValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityInvalidShowConditionError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldLabelCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldMetadataValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldOrderSummaryCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout.GenericLayoutEntityValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityUserRoleValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile.UserProfileValidatorImpl;

public interface ValidationErrorMessageCreator {

    String createErrorMessage(CaseTypeEntityMissingSecurityClassificationValidationError
                                  caseTypeEntityMissingSecurityClassificationValidationError);

    String createErrorMessage(CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                                  classUnderTest);

    String createErrorMessage(CaseFieldEntityMissingSecurityClassificationValidationError classUnderTest);

    String createErrorMessage(ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                                  complexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError);

    String createErrorMessage(ComplexFieldEntityMissingSecurityClassificationValidationError
                                  complexFieldEntityMissingSecurityClassificationValidationError);

    String createErrorMessage(EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError
                                  eventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError);

    String createErrorMessage(EventEntityMissingSecurityClassificationValidationError
                                  eventEntityMissingSecurityClassificationValidationError);

    String createErrorMessage(CreateEventDoesNotHavePostStateValidationError classUnderTest);

    String createErrorMessage(CaseTypeEntityInvalidUserRoleValidationError
                                  caseTypeEntityInvalidUserRoleValidationError);

    String createErrorMessage(CaseTypeEntityInvalidCrudValidationError caseTypeEntityInvalidCrudValidationError);

    String createErrorMessage(CaseFieldEntityInvalidCrudValidationError caseFieldEntityInvalidCrudValidationError);

    String createErrorMessage(CaseFieldEntityInvalidUserRoleValidationError
                                  caseFieldEntityInvalidUserRoleValidationError);

    String createErrorMessage(EventEntityInvalidCrudValidationError eventEntityInvalidCrudValidationError);

    String createErrorMessage(EventEntityInvalidUserRoleValidationError eventEntityInvalidUserRoleValidationError);

    String createErrorMessage(EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError
                                  eventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError);

    String createErrorMessage(EventCaseFieldEntityInvalidShowConditionError
                                  eventCaseFieldEntityShowConditionInvalidError);

    String createErrorMessage(EventEntityMissingForPageTypeDisplayGroupError
                                  eventEntityMissingForPageTypeDisplayGroupError);

    String createErrorMessage(DisplayGroupInvalidShowConditionError displayGroupInvalidShowConditionError);

    String createErrorMessage(DisplayGroupInvalidEventFieldShowCondition displayGroupInvalidEventFieldShowCondition);

    String createErrorMessage(DisplayGroupInvalidTabShowCondition displayGroupInvalidTabShowCondition);

    String createErrorMessage(DisplayGroupInvalidTabFieldShowCondition displayGroupInvalidTabFieldShowCondition);

    String createErrorMessage(ComplexFieldInvalidShowConditionError error);

    String createErrorMessage(ComplexFieldShowConditionReferencesInvalidFieldError error);

    String createErrorMessage(DisplayGroupColumnNumberValidator.ValidationError error);

    String createErrorMessage(GenericLayoutEntityValidatorImpl.ValidationError error);

    String createErrorMessage(StateEntityUserRoleValidatorImpl.ValidationError error);

    String createErrorMessage(StateEntityCrudValidatorImpl.ValidationError error);

    String createErrorMessage(UserProfileValidatorImpl.ValidationError error);

    String createErrorMessage(EventCaseFieldDisplayContextValidatorImpl.ValidationError error);

    String createErrorMessage(EventCaseFieldLabelCaseFieldValidator.ValidationError validationError);

    String createErrorMessage(EventCaseFieldOrderSummaryCaseFieldValidator.ValidationError validationError);

    String createErrorMessage(EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator.ValidationError validationError);

    String createErrorMessage(EventCaseFieldMetadataValidatorImpl.ValidationError error);

    String createErrorMessage(CaseFieldEntityInvalidMetadataFieldValidationError error);

    String createErrorMessage(EventEntityCanSaveDraftValidatorImpl.ValidationError error);

    String createErrorMessage(CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError validationError);

    String createErrorMessage(CaseRoleEntityFieldValueValidatorImpl.ValidationError validationError);

    String createErrorMessage(CaseRoleEntityUniquenessValidatorImpl.ValidationError validationError);
}
