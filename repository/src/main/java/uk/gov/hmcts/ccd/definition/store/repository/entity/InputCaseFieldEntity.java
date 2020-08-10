package uk.gov.hmcts.ccd.definition.store.repository.entity;

import uk.gov.hmcts.ccd.definition.store.repository.LayoutSheetType;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Optional;

@MappedSuperclass
public abstract class InputCaseFieldEntity extends GenericLayoutEntity {

    @Column(name = "show_condition")
    private String showCondition;

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    @Override
    public Optional<String> showCondition() {
        return Optional.ofNullable(showCondition);
    }

    @Override
    public LayoutSheetType getLayoutSheetType() {
        return LayoutSheetType.INPUT;
    }
}
