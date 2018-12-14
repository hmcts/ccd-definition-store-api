package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldDisplayContextParameterValidatorImpl implements EventCaseFieldEntityValidator {

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
            String removeBeginingSection = eventCaseFieldEntity.getDisplayContextParameter().indexOf("#LIST(") > -1
                ? eventCaseFieldEntity.getDisplayContextParameter().replace("#LIST(", "") :
                eventCaseFieldEntity.getDisplayContextParameter().replace("#TABLE(", "");
            String[] result = removeBeginingSection.replace(")", "").split(",");

            for (String listCodeElementNames : result) {
                if (!eventCaseFieldEntity.getCaseField().getFieldType().getCollectionFieldType().getComplexFields()
                    .stream().anyMatch(complexField -> complexField.getReference().equals(listCodeElementNames.trim()))) {
                    validationResult.addError(new ValidationError(
                        String.format("ListCodeElement %s display context parameter is not one of the fields in collection", listCodeElementNames.trim())
                    ){});
                }
            }

        }

        return validationResult;
    }

}
