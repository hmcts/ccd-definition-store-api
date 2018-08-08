package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class ShowCondition {

    private final StringBuilder showConditionExprBuilder = new StringBuilder();

    private final List<String> fields = new ArrayList<>();

    // User builder
    private ShowCondition() {
    }

    private void addShowConditionExpression(String showConditionExpression) {
        showConditionExprBuilder.append(showConditionExpression);
    }

    public String getShowConditionExpression() {
        return showConditionExprBuilder.toString();
    }

    private void addField(String field) {
        fields.add(field);
    }

    public List<String> getFields() {
        return fields;
    }

    private boolean hasShowCondition() {
        return showConditionExprBuilder.length() > 0;
    }

    public static class Builder {

        private final ShowCondition showCondition;

        public Builder() {
            showCondition = new ShowCondition();
        }

        public Builder field(String field) {
            showCondition.addField(field);
            return this;
        }

        public Builder showConditionExpression(String showConditionExpression) {
            showCondition.addShowConditionExpression(showConditionExpression);
            return this;
        }

        public boolean hasShowCondition() {
            return showCondition.hasShowCondition();
        }

        public ShowCondition build() {
            return showCondition;
        }

    }

}
