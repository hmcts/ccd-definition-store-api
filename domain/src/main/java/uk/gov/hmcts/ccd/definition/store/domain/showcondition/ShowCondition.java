package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class ShowCondition {

    private final String showConditionExpression;
    private final List<String> fields;

    public ShowCondition(String showConditionExpression, List<String> fields) {
        this.showConditionExpression = showConditionExpression;
        this.fields = fields;
    }

    public String getShowConditionExpression() {
        return showConditionExpression;
    }

    public List<String> getFields() {
        return fields;
    }

    public static class Builder {

        private final StringBuilder showConditionExprBuilder = new StringBuilder();
        private final List<String> fields = new ArrayList<>();

        public Builder() {
        }

        public Builder field(String field) {
            fields.add(field);
            return this;
        }

        public Builder showConditionExpression(String showConditionExpression) {
            showConditionExprBuilder.append(showConditionExpression);
            return this;
        }

        boolean hasShowCondition() {
            return showConditionExprBuilder.length() > 0;
        }

        public ShowCondition build() {
            return new ShowCondition(showConditionExprBuilder.toString(), fields);
        }

    }

}
