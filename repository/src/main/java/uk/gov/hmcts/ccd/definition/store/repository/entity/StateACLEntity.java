package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

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

}
