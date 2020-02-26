package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
public class PageShowConditionValidatorImpl implements DisplayGroupValidator {

    private final ShowConditionParser showConditionParser;
    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public PageShowConditionValidatorImpl(ShowConditionParser showConditionParser,
                                          CaseFieldEntityUtil caseFieldEntityUtil) {
        this.showConditionParser = showConditionParser;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(DisplayGroupEntity displayGroup, List<DisplayGroupEntity> allDisplayGroups) {
        ValidationResult validationResult = new ValidationResult();

        // from Excel CaseEventToFields.PageShowCondition column
        if (preconditions(displayGroup)) {
            ShowCondition showCondition;
            try {
                showCondition = showConditionParser.parseShowCondition(displayGroup.getShowCondition());
            } catch (InvalidShowConditionException e) {
                validationResult.addError(new DisplayGroupInvalidShowConditionError(displayGroup));
                return validationResult;
            }

            List<String> allSubTypePossibilities = caseFieldEntityUtil
                .buildDottedComplexFieldPossibilities(displayGroup.getCaseType().getCaseFields());

            showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
                if (!allSubTypePossibilities.contains(showConditionField)) {
                    validationResult.addError(
                        new DisplayGroupInvalidEventFieldShowCondition(showConditionField, displayGroup));
                }
            });

            showCondition.getFields().forEach(showConditionField -> {
                if (!displayGroup.getEvent().hasField(showConditionField) && !MetadataField.isMetadataField(showConditionField)) {
                    validationResult.addError(new DisplayGroupInvalidEventFieldShowCondition(showConditionField, displayGroup));
                }
            });
        }
        return validationResult;
    }

    private boolean preconditions(DisplayGroupEntity displayGroup) {
        List<Predicate<DisplayGroupEntity>> preconditions = new ArrayList<>();
        preconditions.add(dg -> !StringUtils.isBlank(dg.getShowCondition()));
        preconditions.add(dg -> dg.getType() == DisplayGroupType.PAGE);
        return preconditions.stream().allMatch(p -> p.test(displayGroup));
    }
}
