package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class WizardPageComplexFieldOverride {
    private String complexFieldElementId;
    private String displayContext;
    private String label;
    private String hintText;
    private String showCondition;
    private String defaultValue;
    private Boolean retainHiddenValue;

    @JsonProperty("complex_field_element_id")
    public String getComplexFieldElementId() {
        return complexFieldElementId;
    }

    public void setComplexFieldElementId(String complexFieldElementId) {
        this.complexFieldElementId = complexFieldElementId;
    }

    @JsonProperty("display_context")
    public String getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }

    @JsonProperty("retain_hidden_value")
    public Boolean getRetainHiddenValue() {
        return retainHiddenValue;
    }

    public void setRetainHiddenValue(Boolean retainHiddenValue) {
        this.retainHiddenValue = retainHiddenValue;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("hint_text")
    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    @JsonProperty("default_value")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
