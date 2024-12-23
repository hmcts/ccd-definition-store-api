package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class CaseEventField {

    private String caseFieldId = null;
    private String displayContext = null;
    private String displayContextParameter;
    private Boolean retainHiddenValue;
    private String showCondition;
    private Boolean showSummaryChangeOption = null;
    private Integer showSummaryContentOption = null;
    private String label = null;
    private String hintText = null;
    private Boolean publish;
    private String publishAs;
    private String defaultValue;
    private Boolean nullifyByDefault;
    private List<CaseEventFieldComplex> caseEventFieldComplex = new ArrayList<>();

    /**
     * Foriegn key to CaseField.id.
     **/
    @ApiModelProperty(required = true, value = "Foreign key to CaseField.id")
    @JsonProperty("case_field_id")
    public String getCaseFieldId() {
        return caseFieldId;
    }

    public void setCaseFieldId(String caseFieldId) {
        this.caseFieldId = caseFieldId;
    }

    /**
     * whether this field is optional, mandatory or read only for this event.
     **/
    @ApiModelProperty(value = "whether this field is optional, mandatory or read only for this event")
    @JsonProperty("display_context")
    public String getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }

    /**
     * Show Condition expression for this field.
     **/
    @ApiModelProperty(value = "Show Condition expression for this field")
    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    /**
     * whether field is shown with the change option.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("show_summary_change_option")
    public Boolean getShowSummaryChangeOption() {
        return showSummaryChangeOption;
    }

    public void setShowSummaryChangeOption(final Boolean showSummaryChangeOption) {
        this.showSummaryChangeOption = showSummaryChangeOption;
    }

    /**
     * whether field is shown and if so in what order on the read only section of the final submit page of an event.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("show_summary_content_option")
    public Integer getShowSummaryContentOption() {
        return showSummaryContentOption;
    }

    public void setShowSummaryContentOption(Integer showSummaryContentOption) {
        this.showSummaryContentOption = showSummaryContentOption;
    }

    /**
     * event case field label.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * event case field hint text.
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
     * whether this field is optional, mandatory or read only for this event.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("display_context_parameter")
    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }

    /**
     * whether to retain value of this field, is dependant on there being a show condition.
     **/
    @ApiModelProperty(value = "whether to retain value of this field, is dependant on there being a show condition")
    @JsonProperty("retain_hidden_value")
    public Boolean getRetainHiddenValue() {
        return retainHiddenValue;
    }

    public void setRetainHiddenValue(Boolean retainHiddenValue) {
        this.retainHiddenValue = retainHiddenValue;
    }

    /**
     * whenever we want to publish this event field.
     **/
    @ApiModelProperty(value = "whenever we want to publish this event field")
    @JsonProperty("publish")
    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    /**
     * allows services to define the default value of top level case fields.
     **/
    @ApiModelProperty(value = "allows services to define the default value of top level case fields")
    @JsonProperty("default_value")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * the form of this event field we want to publish.
     **/
    @ApiModelProperty(value = "the form of this event field we want to publish")
    @JsonProperty("publish_as")
    public String getPublishAs() {
        return publishAs;
    }

    public void setPublishAs(String publishAs) {
        this.publishAs = publishAs;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("case_fields_complex")
    public List<CaseEventFieldComplex> getCaseEventFieldComplex() {
        return caseEventFieldComplex;
    }

    public void setCaseEventFieldComplex(List<CaseEventFieldComplex> eventComplexTypeEntities) {
        this.caseEventFieldComplex = eventComplexTypeEntities;
    }

    /**
     * allows services to define nullify by default value (yes/no).
     **/
    @ApiModelProperty(value = "allows services to define nullify by default value for case fields")
    @JsonProperty("nullify_by_default")
    public Boolean getNullifyByDefault() {
        return nullifyByDefault;
    }

    public void setNullifyByDefault(Boolean nullifyByDefault) {
        this.nullifyByDefault = nullifyByDefault;
    }

}
