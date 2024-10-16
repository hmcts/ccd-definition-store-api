package uk.gov.hmcts.ccd.definition.store.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.Objects;

@Table(name = "state_acl")
@Entity
public class StateACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity stateEntity;

    public StateEntity getStateEntity() {
        return stateEntity;
    }

    public void setStateEntity(final StateEntity stateEntity) {
        this.stateEntity = stateEntity;
    }

    @Transient
    private String oid = IdGenerator.createId();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StateACLEntity that = (StateACLEntity) o;
        if (getId() != null) {
            return Objects.equals(getId(), that.getId());
        } else {
            return Objects.equals(getOid(), that.getOid());
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        } else {
            return Objects.hash(getOid());
        }
    }
}
