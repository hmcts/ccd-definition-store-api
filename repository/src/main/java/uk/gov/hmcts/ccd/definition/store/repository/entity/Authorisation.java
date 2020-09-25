package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.persistence.GenerationType.SEQUENCE;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRoleEntity userRole;

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
     * Used for holding userRoleId, so that we can use this in logs if User Role is missing.
     */
    @Transient
    private String userRoleId;

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

    public UserRoleEntity getUserRole() {
        return userRole;
    }

    public void setUserRole(@NotNull final UserRoleEntity userRole) {
        this.userRole = userRole;
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

    public String getUserRoleId() {
        return userRole == null ? userRoleId : userRole.getReference();
    }

    public void setUserRoleId(String userRoleId) {
        this.userRoleId = userRoleId;
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
