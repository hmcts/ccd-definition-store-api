package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.definition.store.repository.PostgreSQLEnumType;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "case_type")
@Entity
@TypeDef(
    name = "pgsql_securityclassification_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type",
        value = "uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification")
)
public class CaseTypeEntity implements Serializable, Versionable {

    private static final long serialVersionUID = 542723327314434924L;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
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
    @JoinColumn(name = "jurisdiction_id", nullable = false)
    private JurisdictionEntity jurisdiction;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true, mappedBy = "caseType")
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<EventEntity> events = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<StateEntity> states = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true, mappedBy = "caseType")
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<CaseFieldEntity> caseFields = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<CaseTypeACLEntity> caseTypeACLEntities = new ArrayList<>();

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<CaseRoleEntity> caseRoles = new ArrayList<>();

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<SearchAliasFieldEntity> searchAliasFields = new ArrayList<>();

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<RoleToAccessProfilesEntity> roleToAccessProfiles = new ArrayList<>();

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

    @Override
    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
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

    public List<CaseTypeACLEntity> getCaseTypeACLEntities() {
        return caseTypeACLEntities;
    }

    public CaseTypeEntity addCaseTypeACL(final CaseTypeACLEntity caseTypeACLEntity) {
        caseTypeACLEntity.setCaseType(this);
        caseTypeACLEntities.add(caseTypeACLEntity);
        return this;
    }

    public CaseTypeEntity addCaseTypeACLEntities(final Collection<CaseTypeACLEntity> caseTypeACLEntities) {
        caseTypeACLEntities.forEach(e -> addCaseTypeACL(e));
        return this;
    }

    public Optional<CaseFieldEntity> findCaseField(String reference) {
        return this.caseFields.stream().filter(cf -> cf.getReference().equals(reference)).findFirst();
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

    public List<SearchAliasFieldEntity> getSearchAliasFields() {
        return searchAliasFields;
    }

    public CaseTypeEntity addSearchAliasFields(List<SearchAliasFieldEntity> searchAliasFields) {
        this.searchAliasFields.addAll(searchAliasFields);
        return this;
    }

    public List<RoleToAccessProfilesEntity> getRoleToAccessProfiles() {
        return roleToAccessProfiles;
    }
}
