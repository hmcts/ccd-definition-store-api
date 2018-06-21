package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Table(name = "state")
@Entity
public class StateEntity implements Serializable, Referencable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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

    @Column(name = "display_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable=false)
    private CaseTypeEntity caseType;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "state_id")
    private final List<StateUserRoleEntity> stateUserRoles = new ArrayList<>();

    public Integer getId() {
        return id;
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

    public void setLiveFrom(LocalDate liveFrom) {
        this.liveFrom = liveFrom;
    }

    public LocalDate getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(LocalDate liveTo) {
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

    public void setCaseType(final CaseTypeEntity caseTypeEntity) {
        this.caseType = caseTypeEntity;
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

    public List<StateUserRoleEntity> getStateUserRoles() {
        return stateUserRoles;
    }

    public StateEntity addStateUserRole(final StateUserRoleEntity stateUserRoleEntity) {
        stateUserRoleEntity.setStateEntity(this);
        stateUserRoles.add(stateUserRoleEntity);
        return this;
    }

    public StateEntity addStateUserRoles(final Collection<StateUserRoleEntity> entities) {
        entities.forEach(e -> addStateUserRole(e));
        return this;
    }
}
