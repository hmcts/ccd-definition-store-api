package uk.gov.hmcts.ccd.definition.store.repository.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class EventPostState {

    private String enablingCondition;

    private Integer priority;

    private String postStateReference;

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
}
