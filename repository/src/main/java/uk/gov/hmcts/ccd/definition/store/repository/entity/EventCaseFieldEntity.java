package uk.gov.hmcts.ccd.definition.store.repository.entity;

import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "event_case_field")
@Entity
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

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "event_case_field_id")
    private final List<EventComplexTypeEntity> eventComplexTypes = new ArrayList<>();

    @Column(name = "display_context", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
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

    @Column(name = "retain_hidden_value")
    private Boolean retainHiddenValue;

    @Column(name = "publish")
    private Boolean publish;

    @Column(name = "publish_as")
    private String publishAs;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "nullify_by_default")
    private Boolean nullifyByDefault;

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

    public Boolean getRetainHiddenValue() {
        return retainHiddenValue;
    }

    public void setRetainHiddenValue(Boolean retainHiddenValue) {
        this.retainHiddenValue = retainHiddenValue;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public String getPublishAs() {
        return publishAs;
    }

    public void setPublishAs(String publishAs) {
        this.publishAs = publishAs;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getNullifyByDefault() {
        return nullifyByDefault;
    }

    public void setNullifyByDefault(Boolean nullifyByDefault) {
        this.nullifyByDefault = nullifyByDefault;
    }
}
