package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class CaseField implements HasAcls {

    private String id = null;
    private String caseTypeId = null;
    private String label = null;
    private String hintText = null;
    private FieldType fieldType = null;
    private Boolean hidden = null;
    private String securityClassification = null;
    private String liveFrom = null;
    private String liveUntil = null;
    private List<AccessControlList> acls = new ArrayList<>();
    private String showCondition;
    private boolean metadata;

    /**
     * The id of the case field
     **/
    @ApiModelProperty(required = true, value = "The id of the case field")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Foriegn key to the case type as fields should not work across
     **/
    @ApiModelProperty(value = "Foriegn key to the case type as fields should not work across")
    @JsonProperty("case_type_id")
    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    /**
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("hint_text")
    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    /**
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("field_type")
    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("hidden")
    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Gov security level of the data (official, top secret etc)
     **/
    @ApiModelProperty(value = "Gov security level of the data (official, top secret etc)")
    @JsonProperty("security_classification")
    public String getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(String securityClassification) {
        this.securityClassification = securityClassification;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("live_from")
    public String getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(String liveFrom) {
        this.liveFrom = liveFrom;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("live_until")
    public String getLiveUntil() {
        return liveUntil;
    }

    public void setLiveUntil(String liveUntil) {
        this.liveUntil = liveUntil;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("acls")
    public List<AccessControlList> getAcls() {
        return this.acls;
    }

    @Override
    public void setAcls(List<AccessControlList> acls) {
        this.acls = acls;
    }

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }
}
