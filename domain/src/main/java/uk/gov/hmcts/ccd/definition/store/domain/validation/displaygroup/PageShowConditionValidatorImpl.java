package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

@Component
public class PageShowConditionValidatorImpl implements DisplayGroupValidator {

    private final ShowConditionParser showConditionParser;

    @Autowired
    public PageShowConditionValidatorImpl(ShowConditionParser showConditionParser) {
        this.showConditionParser = showConditionParser;
    }

    @Override
    public ValidationResult validate(DisplayGroupEntity displayGroup, Collection<DisplayGroupEntity> allDisplayGroups) {
        ValidationResult validationResult = new ValidationResult();

        if (preconditions(displayGroup)) {
            ShowCondition showCondition;
            try {
                showCondition = showConditionParser.parseShowCondition(displayGroup.getShowCondition());
            } catch (InvalidShowConditionException e) {
                validationResult.addError(new DisplayGroupInvalidShowConditionError(displayGroup));
                return validationResult;
            }

            String showConditionField = showCondition.getField();
            if (!displayGroup.getEvent().hasField(showConditionField)) {
                validationResult.addError(new DisplayGroupInvalidEventFieldShowCondition(showConditionField, displayGroup));
            }
        }
        return validationResult;
    }

    public boolean preconditions(DisplayGroupEntity displayGroup) {
        List<Predicate<DisplayGroupEntity>> preconditions = new ArrayList<>();
        preconditions.add(dg -> !StringUtils.isBlank(dg.getShowCondition()));
        preconditions.add(dg -> dg.getType() == DisplayGroupType.PAGE);
        return preconditions.stream().allMatch(p -> p.test(displayGroup));
    }
}
