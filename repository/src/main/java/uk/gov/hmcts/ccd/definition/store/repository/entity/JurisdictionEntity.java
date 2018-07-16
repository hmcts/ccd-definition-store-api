package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "jurisdiction")
@Entity
public class JurisdictionEntity implements Serializable, Versionable {

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
    private Date liveFrom;

    @Column(name = "live_to")
    private Date liveTo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(fetch = EAGER, cascade = ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "jurisdiction_id")
    private final List<CaseTypeEntity> caseTypes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(final Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    public Date getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(final Date liveTo) {
        this.liveTo = liveTo;
    }

    public void addCaseType(@NotNull final CaseTypeEntity caseType) {
        caseType.setJurisdiction(this);
        caseTypes.add(caseType);
    }

    public void addCaseTypes(@NotNull final Collection<CaseTypeEntity> caseTypes) {
        for (CaseTypeEntity caseType : caseTypes) {
            addCaseType(caseType);
        }
    }

    public List<CaseTypeEntity> getCaseTypes() {
        return caseTypes;
    }

    @Override
    public String toString() {
        return "JurisdictionEntity{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", reference='" + reference + '\'' +
                ", version=" + version +
                ", liveFrom=" + liveFrom +
                ", liveTo=" + liveTo +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
