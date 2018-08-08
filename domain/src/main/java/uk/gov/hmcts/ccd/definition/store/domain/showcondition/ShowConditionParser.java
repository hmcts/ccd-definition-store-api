package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ShowConditionParser {
    private static final String AND_CONDITION_REGEX = "\\s\\.AND\\.\\s";
    private static final String AND_OPERATOR = " .AND. ";
    private static final Pattern EQUALITY_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(=)\\s*?(\".*\")\\s*?");

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            String[] andConditions = rawShowConditionString.split(AND_CONDITION_REGEX);
            ShowCondition.Builder showConditionBuilder = new ShowCondition.Builder();
            String andOperator = "";

            for (String andCondition : andConditions) {
                Matcher matcher = EQUALITY_CONDITION_PATTERN.matcher(andCondition);
                if (matcher.find()) {
                    showConditionBuilder
                        .showConditionExpression(andOperator + matcher.group(1).trim() + matcher.group(2).trim() + matcher.group(3).trim())
                        .field(matcher.group(1).trim());
                    andOperator = AND_OPERATOR;
                }
            }
            if (showConditionBuilder.hasShowCondition()) {
                return showConditionBuilder.build();
            }
        } catch (Exception e) {
            // Do nothing; we're throwing InvalidShowConditionException below
        }
        throw new InvalidShowConditionException(rawShowConditionString);
    }

}
