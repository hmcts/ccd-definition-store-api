package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Fetch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.FetchMode.SUBSELECT;

/**
 * A "lite" version of the {@link CaseTypeEntity} class that contains selected Case Type fields (id, reference, name,
 * description, and list of states) for display purposes. (Class introduced to avoid loading the whole CaseTypeEntity,
 * which is too expensive to do for all Case Types of a given Jurisdiction.)
 */
@Table(name = "case_type")
@Entity
public class CaseTypeLiteEntity implements Serializable, Versionable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "jurisdiction_id", nullable = false)
    private JurisdictionEntity jurisdiction;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<StateEntity> states = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<EventLiteEntity> events = new ArrayList<>();

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = SUBSELECT)
    @JoinColumn(name = "case_type_id")
    private final List<CaseTypeLiteACLEntity> caseTypeLiteACLEntities = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
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

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(final JurisdictionEntity jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public List<StateEntity> getStates() {
        return states;
    }

    public CaseTypeLiteEntity addState(@NotNull final StateEntity state) {
        states.add(state);
        return this;
    }

    public List<EventLiteEntity> getEvents() {
        return events;
    }

    public CaseTypeLiteEntity addEvent(@NotNull final EventLiteEntity event) {
        events.add(event);
        return this;
    }

    public List<CaseTypeLiteACLEntity> getCaseTypeLiteACLEntities() {
        return caseTypeLiteACLEntities;
    }

    public CaseTypeLiteEntity addCaseTypeACL(final CaseTypeLiteACLEntity caseTypeLiteACLEntity) {
        caseTypeLiteACLEntities.add(caseTypeLiteACLEntity);
        return this;
    }

    public CaseTypeLiteEntity addCaseTypeACLs(final Collection<CaseTypeLiteACLEntity> caseTypeLiteACLEntities) {
        caseTypeLiteACLEntities.forEach(e -> addCaseTypeACL(e));
        return this;
    }
}
