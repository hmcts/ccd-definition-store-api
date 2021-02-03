package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Table(name = "event_case_field_complex_type")
@Entity
@TypeDef(
    name = "pgsql_displaycontext_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @org.hibernate.annotations.Parameter(name = "type",
        value = "uk.gov.hmcts.ccd.definition.store.repository.DisplayContext")
)
public class EventComplexTypeEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @Column(name = "hint")
    private String hint;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "default_value", nullable = true)
    private String defaultValue;

    @Column(name = "display_order")
    private Integer order;

    @Column(name = "display_context", nullable = false)
    @Type(type = "pgsql_displaycontext_enum")
    private DisplayContext displayContext;

    @Column(name = "show_condition")
    private String showCondition;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "event_case_field_id", nullable = false)
    private EventCaseFieldEntity complexFieldType;

    @Column(name = "publish")
    private Boolean publish;

    @Column(name = "publish_as")
    private String publishAs;

    @Column(name = "retain_hidden_value")
    private Boolean retainHiddenValue;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(LocalDate liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDate getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(LocalDate liveTo) {
        this.liveTo = liveTo;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public DisplayContext getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(DisplayContext displayContext) {
        this.displayContext = displayContext;
    }

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    public EventCaseFieldEntity getComplexFieldType() {
        return complexFieldType;
    }

    public void setComplexFieldType(EventCaseFieldEntity complexFieldType) {
        this.complexFieldType = complexFieldType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public Boolean getRetainHiddenValue() {
        return retainHiddenValue;
    }

    public void setRetainHiddenValue(Boolean retainHiddenValue) {
        this.retainHiddenValue = retainHiddenValue;
    }
}
