package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;

@Component
public class DisplayGroupDisplayContextParamValidator implements DisplayGroupCaseFieldValidator {

    public static final String LIST_PREFIX = "#LIST(";
    public static final String TABLE_PREFIX = "#TABLE(";

    @Override
    public ValidationResult validate(DisplayGroupCaseFieldEntity entity) {
        ValidationResult validationResult = new ValidationResult();

        if (Strings.isNullOrEmpty(entity.getDisplayContextParameter()) || isDisplayContextParameterDateTimeType(entity)) {
            return validationResult;
        }

        if (isFieldTypeNotCollection(entity)) {

            validationResult.addError(new ValidationError("Display context parameter is not of type collection") {
            });
        }
        if (StringUtils.isNotBlank(entity.getDisplayContextParameter())) {

            if (isFieldTypeNotTableOrList(entity)) {
                validationResult.addError(new ValidationError("DisplayContextParameter text should begin with #LIST(, #TABLE(, #DATETIMEENTRY( or #DATETIMEDISPLAY("){});
            } else {
                String removeBeginingSection = entity.getDisplayContextParameter().indexOf(LIST_PREFIX) > -1
                    ? entity.getDisplayContextParameter().replace(LIST_PREFIX, "") :
                    entity.getDisplayContextParameter().replace(TABLE_PREFIX, "");
                String[] result = removeBeginingSection.replace(")", "").split(",");

                verifyListCodeElements(entity, validationResult, result);
            }

        }

        return validationResult;
    }

    private void verifyListCodeElements(DisplayGroupCaseFieldEntity eventCaseFieldEntity, ValidationResult validationResult, String[] listCodeElementNames) {
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

    private boolean isDisplayContextParameterDateTimeType(DisplayGroupCaseFieldEntity entity) {
        return DisplayContextParameterType.getParameterTypeFor(entity.getDisplayContextParameter())
            .map(t -> t == DisplayContextParameterType.DATETIMEDISPLAY || t == DisplayContextParameterType.DATETIMEENTRY).orElse(false);
    }

    private boolean isFieldTypeNotTableOrList(DisplayGroupCaseFieldEntity entity) {
        return !entity.getDisplayContextParameter().startsWith(LIST_PREFIX)
            && !entity.getDisplayContextParameter().startsWith(TABLE_PREFIX);
    }

    private boolean isFieldTypeNotCollection(DisplayGroupCaseFieldEntity entity) {
        return StringUtils.isNotBlank(entity.getDisplayContextParameter())
            && !FieldTypeUtils.BASE_COLLECTION.equals(entity.getCaseField().getBaseTypeString());
    }

}
