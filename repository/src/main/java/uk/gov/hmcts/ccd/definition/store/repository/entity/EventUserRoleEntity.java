package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "event_user_role")
@Entity
public class EventUserRoleEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable=false)
    private EventEntity event;

    public EventEntity getEvent() {
        return event;
    }

    public void setEventEntity(final EventEntity event) {
        this.event = event;
    }
}
