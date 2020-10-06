package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TabShowConditionValidatorImpl implements DisplayGroupValidator {

    private final ShowConditionParser showConditionParser;
    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public TabShowConditionValidatorImpl(ShowConditionParser showConditionParser,
                                         CaseFieldEntityUtil caseFieldEntityUtil) {
        this.showConditionParser = showConditionParser;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(DisplayGroupEntity thisDisplayGroup, List<DisplayGroupEntity> allDisplayGroups) {
        ValidationResult validationResult = new ValidationResult();
        List<DisplayGroupEntity> allTabDisplayGroups = getAllTabDisplayGroups(thisDisplayGroup, allDisplayGroups);

        // Excel CaseTypeTab.TabShowCondition
        if (hasTabShowCondition(thisDisplayGroup)) {
            ShowCondition showCondition;
            try {
                showCondition = showConditionParser.parseShowCondition(thisDisplayGroup.getShowCondition());
            } catch (InvalidShowConditionException e) {
                validationResult.addError(new DisplayGroupInvalidTabShowCondition(thisDisplayGroup));
                return validationResult;
            }

            List<String> allSubTypePossibilities = caseFieldEntityUtil
                .buildDottedComplexFieldPossibilities(thisDisplayGroup.getCaseType().getCaseFields().stream()
                    .map(FieldEntity.class::cast).collect(Collectors.toList()));

            showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
                if (!allSubTypePossibilities.contains(showConditionField)) {
                    validationResult.addError(
                        new DisplayGroupInvalidTabShowCondition(showConditionField, thisDisplayGroup));
                }
            });

            showCondition.getFields().forEach(showConditionField -> {
                if (!isInTabDisplayGroups(allTabDisplayGroups, showConditionField)
                    && !MetadataField.isMetadataField(showConditionField)) {
                    validationResult.addError(
                        new DisplayGroupInvalidTabShowCondition(showConditionField, thisDisplayGroup));
                }
            });
        }

        // Excel CaseTypeTab.FieldShowCondition
        if (hasFieldShowCondition(thisDisplayGroup)) {
            for (DisplayGroupCaseFieldEntity caseField : thisDisplayGroup.getDisplayGroupCaseFields()) {
                if (caseField.getShowCondition() != null) {
                    ShowCondition showCondition;
                    try {
                        showCondition = showConditionParser.parseShowCondition(caseField.getShowCondition());
                    } catch (InvalidShowConditionException e) {
                        validationResult.addError(new DisplayGroupInvalidTabFieldShowCondition(caseField));
                        return validationResult;
                    }

                    List<String> allSubTypePossibilities = caseFieldEntityUtil
                        .buildDottedComplexFieldPossibilities(thisDisplayGroup.getCaseType().getCaseFields());

                    showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
                        if (!allSubTypePossibilities.contains(showConditionField)) {
                            validationResult.addError(
                                new DisplayGroupInvalidTabFieldShowCondition(showConditionField, caseField));
                        }
                    });

                    showCondition.getFields().forEach(showConditionField -> {
                        if (!isInTabDisplayGroups(allTabDisplayGroups, showConditionField)
                            && !MetadataField.isMetadataField(showConditionField)) {
                            validationResult.addError(
                                new DisplayGroupInvalidTabFieldShowCondition(showConditionField, caseField));
                        }
                    });
                }
            }
        }
        return validationResult;
    }

    private List<DisplayGroupEntity> getAllTabDisplayGroups(DisplayGroupEntity thisDisplayGroup,
                                                            Collection<DisplayGroupEntity> allDisplayGroups) {
        return allDisplayGroups
            .stream()
            .filter(dg -> dg.getType() == DisplayGroupType.TAB)
            .filter(dg -> dg.getCaseType().getReference().equals(thisDisplayGroup.getCaseType().getReference()))
            .collect(Collectors.toList());
    }

    private boolean isInTabDisplayGroups(List<DisplayGroupEntity> tabDisplayGroups, String showConditionField) {
        return tabDisplayGroups.stream().anyMatch(tdg -> tdg.hasField(showConditionField));
    }

    private boolean hasTabShowCondition(DisplayGroupEntity displayGroup) {
        List<Predicate<DisplayGroupEntity>> preconditions = new ArrayList<>();
        preconditions.add(DisplayGroupEntity::hasShowCondition);
        preconditions.add(dg -> dg.getType() == DisplayGroupType.TAB);
        return preconditions.stream().allMatch(p -> p.test(displayGroup));
    }

    private boolean hasFieldShowCondition(DisplayGroupEntity displayGroup) {
        List<Predicate<DisplayGroupEntity>> preconditions = new ArrayList<>();
        preconditions.add(dg -> !isAllDisplayGroupCaseFieldsShowConditionBlank(dg));
        preconditions.add(dg -> dg.getType() == DisplayGroupType.TAB);
        return preconditions.stream().allMatch(p -> p.test(displayGroup));
    }

    private boolean isAllDisplayGroupCaseFieldsShowConditionBlank(DisplayGroupEntity dg) {
        return dg.getDisplayGroupCaseFields().stream().noneMatch(DisplayGroupCaseFieldEntity::hasShowCondition);
    }
}
