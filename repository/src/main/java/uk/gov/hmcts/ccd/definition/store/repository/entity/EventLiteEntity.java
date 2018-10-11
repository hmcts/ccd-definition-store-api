package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.FetchMode.SUBSELECT;

/**
 * A "lite" version of the {@link EventEntity} class that contains selected Event fields (id, reference, name, and
 * description) for display purposes. (Class introduced to avoid loading the whole EventEntity, which is unnecessary.)
 */
@Table(name = "event")
@Entity
public class EventLiteEntity implements Serializable, Referencable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "can_create", nullable = false)
    private Boolean canCreate = Boolean.FALSE;

    @ManyToMany(fetch = EAGER)
    @Fetch(value = SUBSELECT)
    @JoinTable(
        name = "event_pre_state",
        joinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id")
    )
    private final List<StateLiteEntity> preStates = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StateLiteEntity> getPreStates() {
        return preStates;
    }
}
