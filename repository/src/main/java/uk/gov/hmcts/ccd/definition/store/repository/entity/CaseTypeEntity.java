package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

@Table(name = "case_type")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name="type", value="uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")
)
public class CaseTypeEntity implements Serializable, Versionable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= IDENTITY)
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "security_classification")
    @Type(type = "pgsql_securityclassification_enum")
    private SecurityClassification securityClassification;

    @ManyToOne(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "print_webhook_id", nullable = true)
    private WebhookEntity printWebhook;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "jurisdiction_id", nullable=false)
    private JurisdictionEntity jurisdiction;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<EventEntity> events = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<StateEntity> states = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<CaseFieldEntity> caseFields = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<CaseTypeUserRoleEntity> caseTypeUserRoleEntities = new ArrayList<>();

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<CaseRoleEntity> caseRoles = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CaseTypeEntity addEvent(@NotNull final EventEntity event) {
        event.setCaseType(this);
        events.add(event);
        return this;
    }

    public CaseTypeEntity addEvents(@NotNull final Collection<EventEntity> events) {
        for (EventEntity event : events) {
            addEvent(event);
        }
        return this;
    }

    public CaseTypeEntity addState(@NotNull final StateEntity state) {
        state.setCaseType(this);
        states.add(state);
        return this;
    }

    public CaseTypeEntity addStates(@NotNull final Collection<StateEntity> states) {
        for (StateEntity state : states) {
            addState(state);
        }
        return this;
    }

    public CaseTypeEntity addCaseField(@NotNull final CaseFieldEntity caseField) {
        caseField.setCaseType(this);
        caseFields.add(caseField);
        return this;
    }

    public CaseTypeEntity addCaseFields(@NotNull final Collection<CaseFieldEntity> caseFields) {
        for (CaseFieldEntity caseField : caseFields) {
            addCaseField(caseField);
        }
        return this;
    }

    public List<EventEntity> getEvents() {
        return events;
    }

    public List<StateEntity> getStates() {
        return states;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
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

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    public WebhookEntity getPrintWebhook() {
        return printWebhook;
    }

    public void setPrintWebhook(WebhookEntity printWebhook) {
        this.printWebhook = printWebhook;
    }

    public List<CaseFieldEntity> getCaseFields() {
        return caseFields;
    }

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(final JurisdictionEntity jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public List<CaseTypeUserRoleEntity> getCaseTypeUserRoleEntities() {
        return caseTypeUserRoleEntities;
    }

    public CaseTypeEntity addCaseTypeUserRole(final CaseTypeUserRoleEntity caseTypeUserRoleEntity) {
        caseTypeUserRoleEntity.setCaseType(this);
        caseTypeUserRoleEntities.add(caseTypeUserRoleEntity);
        return this;
    }

    public CaseTypeEntity addCaseTypeUserRoles(final Collection<CaseTypeUserRoleEntity> caseTypeUserRoleEntities) {
        caseTypeUserRoleEntities.forEach(e -> addCaseTypeUserRole(e));
        return this;
    }

    public List<CaseRoleEntity> getCaseRoles() {
        return caseRoles;
    }

    public CaseTypeEntity addCaseRole(final CaseRoleEntity caseRoleEntity) {
        caseRoleEntity.setCaseType(this);
        caseRoles.add(caseRoleEntity);
        return this;
    }

    public CaseTypeEntity addCaseRoles(final Collection<CaseRoleEntity> caseRoleEntities) {
        caseRoleEntities.forEach(cr -> addCaseRole(cr));
        return this;
    }
}
