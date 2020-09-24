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

    @Column(name = "matching_condition", nullable = false)
    private String matchingCondition;

    @Column(name = "state_priority")
    private Integer statePriority;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_event_id", nullable = false)
    private EventEntity eventEntity;

    @Column(name = "post_state_reference")
    private String postStateReference;

    public Integer getId() {
        return id;
    }

    public String getMatchingCondition() {
        return matchingCondition;
    }

    public void setMatchingCondition(String matchingCondition) {
        this.matchingCondition = matchingCondition;
    }

    public int getStatePriority() {
        return statePriority;
    }

    public void setStatePriority(int statePriority) {
        this.statePriority = statePriority;
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
