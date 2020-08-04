package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Table(name = "event_case_field")
@Entity
@TypeDef(
    name = "pgsql_displaycontext_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @org.hibernate.annotations.Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.DisplayContext")
)
public class EventCaseFieldEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_case_field_id_seq")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_field_id", nullable = false)
    private CaseFieldEntity caseField;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "event_case_field_id")
    private final List<EventComplexTypeEntity> eventComplexTypes = new ArrayList<>();

    @Column(name = "display_context", nullable = false)
    @Type(type = "pgsql_displaycontext_enum")
    private DisplayContext displayContext;

    @Column(name = "display_context_parameter")
    private String displayContextParameter;

    @Column(name = "show_condition")
    private String showCondition;

    @Column(name = "show_summary_change_option")
    private Boolean showSummaryChangeOption;

    @Column(name = "show_summary_content_option")
    private Integer showSummaryContentOption;

    @Column(name = "label")
    private String label;

    @Column(name = "hint_text")
    private String hintText;

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

    public List<EventComplexTypeEntity> getEventComplexTypes() {
        return eventComplexTypes;
    }

    public void addComplexFields(List<EventComplexTypeEntity> complexFields) {
        if (complexFields != null) {
            for (EventComplexTypeEntity eventComplexTypeEntity : complexFields) {
                eventComplexTypeEntity.setComplexFieldType(this);
                this.eventComplexTypes.add(eventComplexTypeEntity);
            }
        }
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }
}
