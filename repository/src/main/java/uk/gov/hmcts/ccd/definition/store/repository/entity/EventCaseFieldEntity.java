package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "event_case_field")
@Entity
@TypeDef(
    name = "pgsql_displaycontext_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @org.hibernate.annotations.Parameter(name="type", value="uk.gov.hmcts.ccd.definition.store.repository.DisplayContext")
)
public class EventCaseFieldEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable=false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_field_id", nullable=false)
    private CaseFieldEntity caseField;

    @Column(name = "display_context", nullable=false)
    @Type( type = "pgsql_displaycontext_enum" )
    private DisplayContext displayContext;

    @Column(name = "show_condition")
    private String showCondition;

    @Column(name = "show_summary_change_option")
    private Boolean showSummaryChangeOption;

    @Column(name = "show_summary_content_option")
    private Integer showSummaryContentOption;

    public DisplayContext getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(DisplayContext displayContext) {
        this.displayContext = displayContext;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(final EventEntity event) {
        this.event = event;
    }

    public CaseFieldEntity getCaseField() {
        return caseField;
    }

    public void setCaseField(final CaseFieldEntity caseField) {
        this.caseField = caseField;
    }

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    public Boolean getShowSummaryChangeOption() {
        return showSummaryChangeOption;
    }

    public void setShowSummaryChangeOption(final Boolean showSummaryChangeOption) {
        this.showSummaryChangeOption = showSummaryChangeOption;
    }

    public Integer getShowSummaryContentOption() {
        return showSummaryContentOption;
    }

    public void setShowSummaryContentOption(Integer showSummaryContentOption) {
        this.showSummaryContentOption = showSummaryContentOption;
    }
}
