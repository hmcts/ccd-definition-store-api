package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

@Table(name = "display_group_case_field")
@Entity
public class DisplayGroupCaseFieldEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "display_group_case_field_id_seq")
    private Integer id;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @ManyToOne
    @JoinColumn(name = "case_field_id", referencedColumnName = "id")
    private CaseFieldEntity caseField;

    @ManyToOne
    @JoinColumn(name = "display_group_id", referencedColumnName = "id")
    private DisplayGroupEntity displayGroup;

    @Column(name = "display_context_parameter")
    private String displayContextParameter;

    @Column(name = "display_order")
    private Integer order;

    @Column(name = "page_column_no")
    private Integer columnNumber;

    @Column(name = "show_condition")
    private String showCondition;

    public LocalDate getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(final LocalDate liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDate getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(final LocalDate liveTo) {
        this.liveTo = liveTo;
    }

    public DisplayGroupEntity getDisplayGroup() {
        return displayGroup;
    }

    public void setDisplayGroup(@NotNull final DisplayGroupEntity displayGroup) {
        this.displayGroup = displayGroup;
    }

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(@NotNull final CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    public String getShowCondition() {
        return showCondition;
    }

    public boolean hasShowCondition() {
        return !StringUtils.isBlank(showCondition);
    }

    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }
}
