package uk.gov.hmcts.ccd.definition.store.repository.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class NoCConfig {

    private String id = null;
    private Boolean reasonsRequired;
    private Boolean nocActionInterpretationRequired = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getReasonsRequired() {
        return reasonsRequired;
    }

    public void setReasonsRequired(Boolean reasonsRequired) {
        this.reasonsRequired = reasonsRequired;
    }

    public Boolean getNocActionInterpretationRequired() {
        return nocActionInterpretationRequired;
    }

    public void setNocActionInterpretationRequired(Boolean nocActionInterpretationRequired) {
        this.nocActionInterpretationRequired = nocActionInterpretationRequired;
    }
}
