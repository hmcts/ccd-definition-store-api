package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

public class ShowCondition {

    private String showConditionExpression;

    private String field;

    // User builder
    private ShowCondition() {

    }

    public String getField() {
        return this.field;
    }

    public String getShowConditionExpression() {
        return this.showConditionExpression;
    }

    public static class Builder {

        private ShowCondition showCondition;

        public Builder() {
            this.showCondition = new ShowCondition();
        }

        public Builder field(String field) {
            this.showCondition.field = field;
            return this;
        }

        public Builder showConditionExpression(String showConditionExpression) {
            this.showCondition.showConditionExpression = showConditionExpression;
            return this;
        }

        public ShowCondition build() {
            return showCondition;
        }

    }

}
