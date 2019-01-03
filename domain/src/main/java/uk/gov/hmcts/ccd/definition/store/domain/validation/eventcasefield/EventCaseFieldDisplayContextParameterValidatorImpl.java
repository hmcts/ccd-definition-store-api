package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldDisplayContextParameterValidatorImpl implements EventCaseFieldEntityValidator {

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
        if (StringUtils.isNotBlank(eventCaseFieldEntity.getDisplayContextParameter())
            && !FieldTypeUtils.BASE_COLLECTION.equals(eventCaseFieldEntity.getCaseField().getBaseTypeString())) {

            validationResult.addError(new ValidationError("Display context parameter is not of type collection") {
            });
        }
        if (StringUtils.isNotBlank(eventCaseFieldEntity.getDisplayContextParameter())) {

            if (!eventCaseFieldEntity.getDisplayContextParameter().startsWith("#LIST(")
                && !eventCaseFieldEntity.getDisplayContextParameter().startsWith("#TABLE(")) {
                validationResult.addError(new ValidationError("DisplayContextParameter text should begin with #LIST( or #TABLE("){});
            } else {
                String removeBeginingSection = eventCaseFieldEntity.getDisplayContextParameter().indexOf("#LIST(") > -1
                    ? eventCaseFieldEntity.getDisplayContextParameter().replace("#LIST(", "") :
                    eventCaseFieldEntity.getDisplayContextParameter().replace("#TABLE(", "");
                String[] result = removeBeginingSection.replace(")", "").split(",");

                for (String listCodeElementNames : result) {
                    if (eventCaseFieldEntity.getCaseField().getFieldType().getCollectionFieldType().getComplexFields()
                        .stream().noneMatch(complexField -> complexField.getReference().equals(listCodeElementNames.trim()))) {
                        validationResult.addError(new ValidationError(
                            String.format("ListCodeElement %s display context parameter is not one of the fields in collection", listCodeElementNames.trim())
                        ) {
                        });
                    }
                }
            }

        }

        return validationResult;
    }

}
