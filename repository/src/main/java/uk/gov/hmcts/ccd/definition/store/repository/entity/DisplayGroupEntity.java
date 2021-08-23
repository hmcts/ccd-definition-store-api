package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Table(name = "display_group")
@Entity
public class DisplayGroupEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "display_group_id_seq")
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "label")
    private String label;

    @Column(name = "channel")
    private String channel;

    @Column(name = "show_condition")
    private String showCondition;

    @Column(name = "display_order")
    private Integer order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DisplayGroupType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private DisplayGroupPurpose purpose;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @OneToMany(mappedBy = "displayGroup", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    @OrderBy("id")
    private final Set<DisplayGroupCaseFieldEntity> displayGroupCaseFields = new LinkedHashSet<>();

    @ManyToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private AccessProfileEntity accessProfile;


    @ManyToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "webhook_mid_event_id")
    private WebhookEntity webhookMidEvent;

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(final String channel) {
        this.channel = channel;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public DisplayGroupType getType() {
        return type;
    }

    public void setType(final DisplayGroupType type) {
        this.type = type;
    }

    public void setPurpose(final DisplayGroupPurpose purpose) {
        this.purpose = purpose;
    }

    public DisplayGroupPurpose getPurpose() {
        return purpose;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }

    public void addDisplayGroupCaseField(@NotNull final DisplayGroupCaseFieldEntity displayGroupField) {
        displayGroupField.setDisplayGroup(this);
        displayGroupCaseFields.add(displayGroupField);
    }

    public List<DisplayGroupCaseFieldEntity> getDisplayGroupCaseFields() {
        return List.copyOf(displayGroupCaseFields);
    }

    public WebhookEntity getWebhookMidEvent() {
        return webhookMidEvent;
    }

    public void setWebhookMidEvent(WebhookEntity webhookMidEvent) {
        this.webhookMidEvent = webhookMidEvent;
    }

    public void addDisplayGroupCaseFields(List<DisplayGroupCaseFieldEntity> groupCaseFields) {
        for (DisplayGroupCaseFieldEntity groupCaseField : groupCaseFields) {
            addDisplayGroupCaseField(groupCaseField);
        }
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

    public boolean hasField(String fieldReference) {
        return displayGroupCaseFields.stream().anyMatch(
            dgcf -> dgcf.getCaseField().getReference().equals(fieldReference));
    }

    public AccessProfileEntity getAccessProfile() {
        return accessProfile;
    }

    public void setAccessProfile(AccessProfileEntity accessProfile) {
        this.accessProfile = accessProfile;
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DisplayGroupEntity that = (DisplayGroupEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(reference, that.reference) && Objects.equals(label, that.label) && Objects.equals(channel, that.channel) && Objects.equals(showCondition, that.showCondition) && Objects.equals(order, that.order) && type == that.type && purpose == that.purpose && Objects.equals(caseType, that.caseType) && Objects.equals(event, that.event) && Objects.equals(displayGroupCaseFields, that.displayGroupCaseFields) && Objects.equals(accessProfile, that.accessProfile) && Objects.equals(webhookMidEvent, that.webhookMidEvent);
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Override
    public int hashCode() {
        return Objects.hash(id, reference, label, channel, showCondition, order, type, purpose, caseType, event, displayGroupCaseFields, accessProfile, webhookMidEvent);
    }
}
