package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GenericLayoutShowConditionValidatorImpl implements GenericLayoutValidator {

    private static final String ERROR_MESSAGE_SHOW_CONDITION =
        "Invalid show condition '%s' for case type '%s' and case field '%s'";
    public static final String UNKNOWN_FIELD_IN_SHOW_CONDITION =
        "Unknown field '%s' for case type '%s' in show condition: '%s'";

    private final ShowConditionParser showConditionExtractor;
    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public GenericLayoutShowConditionValidatorImpl(ShowConditionParser showConditionExtractor, CaseFieldEntityUtil caseFieldEntityUtil) {
        this.showConditionExtractor = showConditionExtractor;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(GenericLayoutEntity entity, List<GenericLayoutEntity> allGenericLayouts) {
        ValidationResult validationResult = new ValidationResult();
        Optional<String> showConditionOptional = entity.showCondition();

        if (!showConditionOptional.isPresent()) {
            return validationResult;
        }
        String showConditionString = showConditionOptional.get();
        ShowCondition showCondition;
        try {
            showCondition = showConditionExtractor.parseShowCondition(showConditionString);
        } catch (InvalidShowConditionException e) {
            validationResult.addError(
                new ValidationError(String.format(ERROR_MESSAGE_SHOW_CONDITION, showConditionString,
                        entity.getCaseType().getReference(), entity.getCaseField().getReference()), entity));
            return validationResult;
        }

        List<String> allSubTypePossibilities = getAllSubTypePossibilities(allGenericLayouts);
        showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
            if (!allSubTypePossibilities.contains(showConditionField)) {
                validationResult.addError(buildError(entity, showConditionField, showConditionString));
            }
        });

        showCondition.getFields().forEach(showConditionField -> {
            if (!showConditionFieldExistsInAtLeastOneLayOutEntity(
                showConditionField, allGenericLayouts)
                && !MetadataField.isMetadataField(showConditionField)) {
                validationResult.addError(buildError(entity, showConditionField, showConditionString));
            }
        });

        return validationResult;
    }

    private List<String> getAllSubTypePossibilities(List<GenericLayoutEntity> layoutEntities) {
        return caseFieldEntityUtil.buildDottedComplexFieldPossibilities(
                layoutEntities.stream()
                    .map(GenericLayoutEntity::getCaseField)
                    .collect(Collectors.toList()));
    }

    private boolean showConditionFieldExistsInAtLeastOneLayOutEntity(
        String showConditionField, List<GenericLayoutEntity> layoutEntities) {
        return layoutEntities
            .stream()
            .anyMatch(layoutEntity -> layoutEntity.getCaseField().getReference().equals(showConditionField));
    }

    private ValidationError buildError(GenericLayoutEntity entity, String showConditionField, String showCondition) {
        return new ValidationError(
            String.format(UNKNOWN_FIELD_IN_SHOW_CONDITION,
                showConditionField,
                entity.getCaseType().getReference(), showCondition), entity);
    }

}
