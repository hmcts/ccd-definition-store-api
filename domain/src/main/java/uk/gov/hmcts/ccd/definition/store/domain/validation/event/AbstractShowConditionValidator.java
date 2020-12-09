package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

abstract class  AbstractShowConditionValidator implements EventEntityValidator {

    private final ShowConditionParser showConditionExtractor;
    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public AbstractShowConditionValidator(final ShowConditionParser showConditionExtractor,
                                          CaseFieldEntityUtil caseFieldEntityUtil) {
        this.showConditionExtractor = showConditionExtractor;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    protected void validateShowConditionFields(EventEntity eventEntity,
                                               ValidationResult validationResult,
                                               String enablingCondition) {
        try {

            ShowCondition showCondition = showConditionExtractor.parseShowCondition(enablingCondition);

            List<String> allSubTypePossibilities = caseFieldEntityUtil
                .buildDottedComplexFieldPossibilities(eventEntity.getCaseType().getCaseFields());

            showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
                if (!allSubTypePossibilities.contains(showConditionField)) {
                    validationResult.addError(getValidationError(
                        showConditionField,
                        eventEntity,
                        enablingCondition
                    ));
                }
            });

            showCondition.getFields().forEach(showConditionField -> {
                if (!forShowConditionFieldExistsAtLeastOneCaseFieldEntity(
                    showConditionField,
                    eventEntity.getCaseType().getCaseFields())
                    && !MetadataField.isMetadataField(showConditionField)) {
                    validationResult.addError(getValidationError(
                        showConditionField,
                        eventEntity,
                        enablingCondition
                    ));
                }
            });
        } catch (InvalidShowConditionException e) {
            // this is handled during parsing. here no exceptions will be thrown
        }
    }

    private boolean forShowConditionFieldExistsAtLeastOneCaseFieldEntity(String showConditionField,
                                                                         List<CaseFieldEntity> caseFieldEntities) {
        return caseFieldEntities
            .stream()
            .anyMatch(f -> f.getReference().equals(showConditionField));
    }

    abstract ValidationError getValidationError(String showConditionField,
                                                EventEntity eventEntity,
                                                String showCondition);
}
