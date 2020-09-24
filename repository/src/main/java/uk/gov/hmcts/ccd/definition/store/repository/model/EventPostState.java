package uk.gov.hmcts.ccd.definition.store.repository.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class EventPostState {

    private String id = null;

    private String matchingCondition;

    private Integer statePriority;

    private String postStateReference;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatchingCondition() {
        return matchingCondition;
    }

    public void setMatchingCondition(String matchingCondition) {
        this.matchingCondition = matchingCondition;
    }

    public Integer getStatePriority() {
        return statePriority;
    }

    public void setStatePriority(Integer statePriority) {
        this.statePriority = statePriority;
    }

    public String getPostStateReference() {
        return postStateReference;
    }

    public void setPostStateReference(String postStateReference) {
        this.postStateReference = postStateReference;
    }
}
