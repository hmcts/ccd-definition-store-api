package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldDisplayContextParameterValidatorImpl implements EventCaseFieldEntityValidator {

    public static final String LIST_PREFIX = "#LIST(";
    public static final String TABLE_PREFIX = "#TABLE(";

    /**
     * Validate event case field entity to contain display context as per #List/TABLE(DisplayContextParameter,DisplayContextParameter) format.
     * @param eventCaseFieldEntity object
     * @param eventCaseFieldEntityValidationContext object
     * @return ValidationResult object
     */
    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();

        if (Strings.isNullOrEmpty(eventCaseFieldEntity.getDisplayContextParameter()) || isDisplayContextParameterDateTimeType(eventCaseFieldEntity)) {
            return validationResult;
        }

        if (isFieldTypeNotCollection(eventCaseFieldEntity)) {

            validationResult.addError(new ValidationError("Display context parameter is not of type collection") {
            });
        }
        if (StringUtils.isNotBlank(eventCaseFieldEntity.getDisplayContextParameter())) {

            if (isFieldTypeNotTableOrList(eventCaseFieldEntity)) {
                validationResult.addError(new ValidationError("DisplayContextParameter text should begin with #LIST( or #TABLE("){});
            } else {
                String removeBeginingSection = eventCaseFieldEntity.getDisplayContextParameter().indexOf(LIST_PREFIX) > -1
                    ? eventCaseFieldEntity.getDisplayContextParameter().replace(LIST_PREFIX, "") :
                    eventCaseFieldEntity.getDisplayContextParameter().replace(TABLE_PREFIX, "");
                String[] result = removeBeginingSection.replace(")", "").split(",");

                verifyListCodeElements(eventCaseFieldEntity, validationResult, result);
            }

        }

        return validationResult;
    }

    private void verifyListCodeElements(EventCaseFieldEntity eventCaseFieldEntity, ValidationResult validationResult, String[] listCodeElementNames) {
        for (String listCodeElementName : listCodeElementNames) {
            if (eventCaseFieldEntity.getCaseField().getFieldType().getCollectionFieldType().getComplexFields()
                .stream().noneMatch(complexField -> complexField.getReference().equals(listCodeElementName.trim()))) {
                validationResult.addError(new ValidationError(
                    String.format("ListCodeElement %s display context parameter is not one of the fields in collection", listCodeElementName.trim())
                ) {
                });
            }
        }
    }

    private boolean isDisplayContextParameterDateTimeType(EventCaseFieldEntity entity) {
        return DisplayContextParameterType.getParameterTypeFor(entity.getDisplayContextParameter())
            .map(t -> t == DisplayContextParameterType.DATETIMEDISPLAY || t == DisplayContextParameterType.DATETIMEENTRY).orElse(false);
    }

    private boolean isFieldTypeNotTableOrList(EventCaseFieldEntity eventCaseFieldEntity) {
        return !eventCaseFieldEntity.getDisplayContextParameter().startsWith(LIST_PREFIX)
            && !eventCaseFieldEntity.getDisplayContextParameter().startsWith(TABLE_PREFIX);
    }

    private boolean isFieldTypeNotCollection(EventCaseFieldEntity eventCaseFieldEntity) {
        return StringUtils.isNotBlank(eventCaseFieldEntity.getDisplayContextParameter())
            && !FieldTypeUtils.BASE_COLLECTION.equals(eventCaseFieldEntity.getCaseField().getBaseTypeString());
    }

}
