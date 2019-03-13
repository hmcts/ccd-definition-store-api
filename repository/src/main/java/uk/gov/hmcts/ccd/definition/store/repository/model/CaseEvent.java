package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

@ApiModel(description = "")
public class CaseEvent implements HasAcls, Serializable {

    private static final long serialVersionUID = -2691601540176003124L;

    private String id = null;
    private String name = null;
    private String description = null;
    private Integer order = null;
    private List<CaseEventField> caseFields = new ArrayList<>();
    private List<String> preStates = new ArrayList<>();
    private String postState = null;
    private String callBackURLAboutToStartEvent;
    private List<Integer> retriesTimeoutAboutToStartEvent;
    private String callBackURLAboutToSubmitEvent;
    private List<Integer> retriesTimeoutURLAboutToSubmitEvent;
    private String callBackURLSubmittedEvent;
    private List<Integer> retriesTimeoutURLSubmittedEvent;
    private SecurityClassification securityClassification;
    private List<AccessControlList> acls = new ArrayList<>();
    private Boolean showSummary = null;
    private Boolean showEventNotes = null;
    private Boolean canSaveDraft = null;
    private String endButtonLabel = null;

    /**
     *
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("case_fields")
    public List<CaseEventField> getCaseFields() {
        return caseFields;
    }

    public void setCaseFields(List<CaseEventField> caseFields) {
        this.caseFields = caseFields;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("pre_states")
    public List<String> getPreStates() {
        return preStates;
    }

    public void setPreStates(List<String> preStates) {
        this.preStates = preStates;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("post_state")
    public String getPostState() {
        return postState;
    }

    public void setPostState(String postState) {
        this.postState = postState;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("callback_url_about_to_start_event")
    public String getCallBackURLAboutToStartEvent() {
        return callBackURLAboutToStartEvent;
    }

    public void setCallBackURLAboutToStartEvent(String callBackURLAboutToStartEvent) {
        this.callBackURLAboutToStartEvent = callBackURLAboutToStartEvent;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("retries_timeout_about_to_start_event")
    public List<Integer> getRetriesTimeoutAboutToStartEvent() {
        return retriesTimeoutAboutToStartEvent == null ? Collections.emptyList() : retriesTimeoutAboutToStartEvent;
    }

    public void setRetriesTimeoutAboutToStartEvent(List<Integer> retriesTimeoutAboutToStartEvent) {
        this.retriesTimeoutAboutToStartEvent = retriesTimeoutAboutToStartEvent;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("callback_url_about_to_submit_event")
    public String getCallBackURLAboutToSubmitEvent() {
        return callBackURLAboutToSubmitEvent;
    }

    public void setCallBackURLAboutToSubmitEvent(String callBackURLAboutToSubmitEvent) {
        this.callBackURLAboutToSubmitEvent = callBackURLAboutToSubmitEvent;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("retries_timeout_url_about_to_submit_event")
    public List<Integer> getRetriesTimeoutURLAboutToSubmitEvent() {
        return retriesTimeoutURLAboutToSubmitEvent == null ? Collections.emptyList() : retriesTimeoutURLAboutToSubmitEvent;
    }

    public void setRetriesTimeoutURLAboutToSubmitEvent(List<Integer> retriesTimeoutURLAboutToSubmitEvent) {
        this.retriesTimeoutURLAboutToSubmitEvent = retriesTimeoutURLAboutToSubmitEvent;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("callback_url_submitted_event")
    public String getCallBackURLSubmittedEvent() {
        return callBackURLSubmittedEvent;
    }

    public void setCallBackURLSubmittedEvent(String callBackURLSubmittedEvent) {
        this.callBackURLSubmittedEvent = callBackURLSubmittedEvent;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("retries_timeout_url_submitted_event")
    public List<Integer> getRetriesTimeoutURLSubmittedEvent() {
        return retriesTimeoutURLSubmittedEvent == null ? Collections.emptyList() : retriesTimeoutURLSubmittedEvent;
    }

    public void setRetriesTimeoutURLSubmittedEvent(List<Integer> retriesTimeoutURLSubmittedEvent) {
        this.retriesTimeoutURLSubmittedEvent = retriesTimeoutURLSubmittedEvent;
    }

    @JsonProperty("security_classification")
    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    @JsonProperty("acls")
    public List<AccessControlList> getAcls() {
        return this.acls;
    }

    @Override
    public void setAcls(List<AccessControlList> acls) {
        this.acls = acls;
    }

    /**
     *
     */
    @ApiModelProperty(value = "")
    @JsonProperty("show_summary")
    public Boolean getShowSummary() {
        return showSummary;
    }

    public void setShowSummary(final Boolean showSummary) {
        this.showSummary = showSummary;
    }

    /**
     *
     */
    @ApiModelProperty(value = "")
    @JsonProperty("show_event_notes")
    public Boolean getShowEventNotes() {
        return showEventNotes;
    }

    public void setShowEventNotes(Boolean showEventNotes) {
        this.showEventNotes = showEventNotes;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("end_button_label")
    public String getEndButtonLabel() {
        return endButtonLabel;
    }

    public void setEndButtonLabel(final String endButtonLabel) {
        this.endButtonLabel = endButtonLabel;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("can_save_draft")
    public Boolean getCanSaveDraft() {
        return canSaveDraft;
    }

    public void setCanSaveDraft(Boolean canSaveDraft) {
        this.canSaveDraft = canSaveDraft;
    }
}
