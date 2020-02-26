package uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter;

public class DisplayContextParameter {

    private DisplayContextParameterType type;

    private String value;

    public DisplayContextParameter(DisplayContextParameterType type, String value) {
        this.type = type;
        this.value = value;
    }

    public DisplayContextParameterType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
