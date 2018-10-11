package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
public abstract class Authorisation {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
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
    @JoinColumn(name = "user_role_id", nullable = false)
    private UserRoleEntity userRole;

    @Column(name = "live_from")
    private LocalDate liveFrom;

    @Column(name = "live_to")
    private LocalDate liveTo;

    /**
     * Used for holding value during parsing.
     */
    @Transient
    private String crudAsString;

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

    public String getCrudAsString() {
        return crudAsString;
    }

    public void setCrudAsString(final String crudAsString) {
        this.crudAsString = crudAsString;
    }
}
