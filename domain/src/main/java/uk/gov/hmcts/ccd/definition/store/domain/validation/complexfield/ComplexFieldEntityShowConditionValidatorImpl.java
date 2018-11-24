package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

/**
 * @deprecated This should not be here. Complex fields will have new optional Excel tab called
 * ComplexTypeToCaseEvent https://tools.hmcts.net/jira/browse/RDM-3327
 */
@Component
@Deprecated
public class ComplexFieldEntityShowConditionValidatorImpl implements ComplexFieldValidator {

    private final ShowConditionParser showConditionExtractor;

    @Autowired
    public ComplexFieldEntityShowConditionValidatorImpl(ShowConditionParser showConditionExtractor) {
        this.showConditionExtractor = showConditionExtractor;
    }

    @Override
    public ValidationResult validate(ComplexFieldEntity complexField, ValidationContext validationContext) {

        ValidationResult validationResult = new ValidationResult();

        if (StringUtils.isBlank(complexField.getShowCondition())) {
            return validationResult;
        }

        ShowCondition showCondition;
        try {
            showCondition = showConditionExtractor.parseShowCondition(complexField.getShowCondition());
        } catch (InvalidShowConditionException e) {
            validationResult.addError(new ComplexFieldInvalidShowConditionError(complexField));
            return validationResult;
        }

        showCondition.getFields().forEach(showConditionField -> {
            if (!complexField.getComplexFieldType().hasComplexField(showConditionField)) {
                validationResult.addError(
                    new ComplexFieldShowConditionReferencesInvalidFieldError(showConditionField, complexField)
                );
            }
        });

        return validationResult;
    }
}
