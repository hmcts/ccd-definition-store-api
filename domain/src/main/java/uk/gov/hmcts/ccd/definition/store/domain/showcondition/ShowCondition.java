package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
public class ShowCondition {

    private final String showConditionExpression;
    private final List<String> fields;

    private ShowCondition(String showConditionExpression, List<String> fields) {
        this.showConditionExpression = showConditionExpression;
        this.fields = fields;
    }

    public String getShowConditionExpression() {
        return showConditionExpression;
    }

    public Set<String> getFields() {
        return fields.stream().map(ShowCondition::dropSubtypes).collect(Collectors.toSet());
    }

    public List<String> getFieldsWithSubtypes() {
        return fields.stream().filter(field -> field.contains(".")).collect(Collectors.toList());
    }

    private static String dropSubtypes(String field) {
        if (field.contains(".")) {
            return field.substring(0, field.indexOf('.'));
        } else {
            return field;
        }
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
