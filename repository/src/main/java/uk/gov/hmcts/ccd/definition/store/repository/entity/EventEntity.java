package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static org.hibernate.annotations.FetchMode.SUBSELECT;

import com.google.common.collect.Maps;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.model.WebhookType;

@Table(name = "event")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")
)
public class EventEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_seq")
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "can_create", nullable = false)
    private Boolean canCreate = Boolean.FALSE;

    @Column(name = "display_order")
    private Integer order;

    @Column(name = "security_classification")
    @Type(type = "pgsql_securityclassification_enum")
    private SecurityClassification securityClassification;

    @Column(name = "show_summary")
    private Boolean showSummary;

    @Column(name = "end_button_label")
    private String endButtonLabel;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "post_state_id")
    private StateEntity postState;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @ManyToMany(fetch = EAGER)
    @Fetch(value = SUBSELECT)
    @JoinTable(
        name = "event_pre_state",
        joinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id")
    )
    private final List<StateEntity> preStates = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = ALL)
    @MapKey(name = "type")
    @MapKeyEnumerated(value = EnumType.STRING)
    @Fetch(value = FetchMode.SUBSELECT)
    private Map<WebhookType, EventWebhookEntity> webhooks = Maps.newHashMap();

    @OneToMany(mappedBy = "event", fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = SUBSELECT)
    private final List<EventCaseFieldEntity> eventCaseFields = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true, mappedBy = "event")
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<EventACLEntity> eventACLEntities = new ArrayList<>();

    @Column(name = "show_event_notes")
    private Boolean showEventNotes;

    @Column(name = "can_save_draft")
    private Boolean canSaveDraft;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
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

    public boolean isCanCreate() {
        return canCreate;
    }

    public boolean getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(final boolean canCreate) {
        this.canCreate = canCreate;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    public Boolean getShowSummary() {
        return showSummary;
    }

    public void setShowSummary(final Boolean showSummary) {
        this.showSummary = showSummary;
    }

    public String getEndButtonLabel() {
        return endButtonLabel;
    }

    public void setEndButtonLabel(String endButtonLabel) {
        this.endButtonLabel = endButtonLabel;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(final CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public StateEntity getPostState() {
        return postState;
    }

    public void setPostState(final StateEntity postState) {
        this.postState = postState;
    }

    public List<StateEntity> getPreStates() {
        return preStates;
    }

    public void addPreState(@NotNull final StateEntity state) {
        preStates.add(state);
    }

    public WebhookEntity getWebhookStart() {
        return getWebhook(WebhookType.START);
    }

    public void setWebhookStart(final WebhookEntity webhookStart) {
        setWebhook(WebhookType.START, webhookStart);
    }

    public WebhookEntity getWebhookPreSubmit() {
        return getWebhook(WebhookType.PRE_SUBMIT);
    }

    public void setWebhookPreSubmit(final WebhookEntity webhookPreSubmit) {
        setWebhook(WebhookType.PRE_SUBMIT, webhookPreSubmit);
    }

    public WebhookEntity getWebhookPostSubmit() {
        return getWebhook(WebhookType.POST_SUBMIT);
    }

    public void setWebhookPostSubmit(final WebhookEntity webhookPostSubmit) {
        setWebhook(WebhookType.POST_SUBMIT, webhookPostSubmit);
    }

    public void addEventCaseField(@NotNull final EventCaseFieldEntity eventCaseField) {
        eventCaseField.setEvent(this);
        eventCaseFields.add(eventCaseField);
    }

    public void addEventCaseFields(@NotNull final Collection<EventCaseFieldEntity> eventCaseFields) {
        for (EventCaseFieldEntity eventCaseField : eventCaseFields) {
            addEventCaseField(eventCaseField);
        }
    }

    public List<EventCaseFieldEntity> getEventCaseFields() {
        return eventCaseFields;
    }

    public List<EventACLEntity> getEventACLEntities() {
        return eventACLEntities;
    }

    public EventEntity addEventACL(final EventACLEntity eventACLEntity) {
        eventACLEntity.setEventEntity(this);
        eventACLEntities.add(eventACLEntity);
        return this;
    }

    public EventEntity addEventACLEntities(final Collection<EventACLEntity> eventACLEntities) {
        eventACLEntities.forEach(e -> addEventACL(e));
        return this;
    }

    public boolean hasField(String fieldReference) {
        return eventCaseFields.stream().anyMatch(ecf -> ecf.getCaseField().getReference().equals(fieldReference));
    }

    public Boolean getShowEventNotes() {
        return showEventNotes;
    }

    public void setShowEventNotes(Boolean showEventNotes) {
        this.showEventNotes = showEventNotes;
    }

    public Boolean getCanSaveDraft() {
        return canSaveDraft;
    }

    public void setCanSaveDraft(Boolean canSaveDraft) {
        this.canSaveDraft = canSaveDraft;
    }

    private void setWebhook(WebhookType type, WebhookEntity webhook) {
        if (null != webhook) {
            webhooks.put(type, new EventWebhookEntity(this, webhook, type));
        }
    }

    private WebhookEntity getWebhook(WebhookType type) {
        EventWebhookEntity ewh = webhooks.get(type);
        return ewh == null ? null : ewh.getWebhook();
    }
}
