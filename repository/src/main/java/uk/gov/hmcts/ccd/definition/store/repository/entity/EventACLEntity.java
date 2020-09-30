package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "event_acl")
@Entity
public class EventACLEntity extends Authorisation implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    public EventEntity getEvent() {
        return event;
    }

    public void setEventEntity(final EventEntity event) {
        this.event = event;
    }
}
