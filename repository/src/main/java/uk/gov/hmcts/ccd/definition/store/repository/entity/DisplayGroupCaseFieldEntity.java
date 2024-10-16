package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    @Transient
    private String oid = IdGenerator.createId();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DisplayGroupCaseFieldEntity that = (DisplayGroupCaseFieldEntity) o;
        if (getId() != null) {
            return Objects.equals(getId(), that.getId());
        } else {
            return Objects.equals(getOid(), that.getOid());
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        } else {
            return Objects.hash(getOid());
        }
    }
}
