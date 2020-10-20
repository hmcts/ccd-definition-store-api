package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@Table(name = "event_post_state")
@Entity
public class EventPostStateEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "enabling_condition", nullable = false)
    private String enablingCondition;

    @Column(name = "priority")
    private Integer priority;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_event_id", nullable = false)
    private EventEntity eventEntity;

    @Column(name = "post_state_reference")
    private String postStateReference;

    public Integer getId() {
        return id;
    }

    public String getEnablingCondition() {
        return enablingCondition;
    }

    public void setEnablingCondition(String enablingCondition) {
        this.enablingCondition = enablingCondition;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPostStateReference() {
        return postStateReference;
    }

    public void setPostStateReference(String postStateReference) {
        this.postStateReference = postStateReference;
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }

    public void setEventEntity(EventEntity eventEntity) {
        this.eventEntity = eventEntity;
    }
}
