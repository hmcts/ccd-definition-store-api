package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

public class InvalidShowConditionException extends Exception {

    private String showCondition;

    public InvalidShowConditionException(String showCondition) {
        this.showCondition = showCondition;
    }

    public String getShowCondition() {
        return this.showCondition;
    }

}
