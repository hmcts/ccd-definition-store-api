package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;

import java.util.List;

/**
 * Composite validator which applies a list of DisplayGroupCaseFieldValidator to all the fields of a DisplayGroupEntity.
 */
@Component
public class DisplayGroupCaseFieldsValidatorImpl implements DisplayGroupValidator {

    private List<DisplayGroupCaseFieldValidator> validators;

    @Autowired
    public DisplayGroupCaseFieldsValidatorImpl(List<DisplayGroupCaseFieldValidator> validators) {
        this.validators = validators;
    }

    @Override
    public ValidationResult validate(DisplayGroupEntity displayGroup, List<DisplayGroupEntity> allDisplayGroups) {
        ValidationResult vr = new ValidationResult();
        for (DisplayGroupCaseFieldEntity e : displayGroup.getDisplayGroupCaseFields()) {
            if (displayGroup.getType() == DisplayGroupType.TAB) {
                for (DisplayGroupCaseFieldValidator v : validators) {
                    vr.merge(v.validate(e));
                }
            }
        }
        return vr;
    }
}
