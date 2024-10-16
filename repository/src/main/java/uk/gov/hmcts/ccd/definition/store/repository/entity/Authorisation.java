package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static jakarta.persistence.GenerationType.SEQUENCE;

@MappedSuperclass
public abstract class Authorisation implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = SEQUENCE, generator = "case_field_acl_id_seq")
    private Integer id;

    @NotNull
    @Column(name = "\"create\"")
    private Boolean create;

    @NotNull
    private Boolean read;

    @NotNull
    private Boolean update;

    @NotNull
    private Boolean delete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private AccessProfileEntity accessProfile;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Used for holding value during parsing.
     */
    @Transient
    private String crudAsString;

    /**
     * Used for holding accessProfileId, so that we can use this in logs if Access Profile is missing.
     */
    @Transient
    private String accessProfileId;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getCreate() {
        return create;
    }

    public void setCreate(@NotNull final Boolean create) {
        this.create = create;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(@NotNull final Boolean read) {
        this.read = read;
    }

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(@NotNull final Boolean update) {
        this.update = update;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(@NotNull final Boolean delete) {
        this.delete = delete;
    }

    public AccessProfileEntity getAccessProfile() {
        return accessProfile;
    }

    public void setAccessProfile(@NotNull final AccessProfileEntity accessProfile) {
        this.accessProfile = accessProfile;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCrudAsString() {
        return crudAsString;
    }

    public void setCrudAsString(final String crudAsString) {
        this.crudAsString = crudAsString;
    }

    public String getAccessProfileId() {
        return accessProfile == null ? accessProfileId : accessProfile.getReference();
    }

    public void setAccessProfileId(String accessProfileId) {
        this.accessProfileId = accessProfileId;
    }

    @Transient
    public boolean hasLowerAccessThan(Authorisation other) {
        boolean hasMoreAccessOnCreate = childHasHigherAccess(this.getCreate(), other.getCreate());
        boolean hasMoreAccessOnRead = childHasHigherAccess(this.getRead(), other.getRead());
        boolean hasMoreAccessOnUpdate = childHasHigherAccess(this.getUpdate(), other.getUpdate());
        boolean hasMoreAccessOnDelete = childHasHigherAccess(this.getDelete(), other.getDelete());

        return hasMoreAccessOnCreate || hasMoreAccessOnRead || hasMoreAccessOnUpdate || hasMoreAccessOnDelete;
    }

    private boolean childHasHigherAccess(Boolean parent, Boolean child) {
        if (isNull(child)) {
            return false;
        } else if (isNull(parent) && nonNull(child)) {
            return true;
        } else if (child.booleanValue() == false || parent.booleanValue() == true) {
            return false;
        }
        return true;
    }
}
