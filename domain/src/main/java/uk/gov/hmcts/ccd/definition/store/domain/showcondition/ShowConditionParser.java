package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ShowConditionParser {

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            Matcher matcher = Pattern.compile("\\s*?(.*)\\s*?(=)\\s*?(\".*\")\\s*?").matcher(rawShowConditionString);
            if (matcher.find()) {
                return new ShowCondition.Builder()
                                .showConditionExpression(matcher.group(1).trim() + matcher.group(2).trim() + matcher.group(3).trim())
                                .field(matcher.group(1).trim())
                                .build();

            }
        }
        catch (Exception e) {
            // Do nothing; we're throwing InvalidShowConditionException below
        }
        throw new InvalidShowConditionException(rawShowConditionString);
    }

}
