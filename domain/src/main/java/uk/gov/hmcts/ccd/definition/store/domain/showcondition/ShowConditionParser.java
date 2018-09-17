package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ShowConditionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ShowConditionParser.class);

    private static final String AND_CONDITION_REGEX = "\\sAND\\s(?=(([^\"]*\"){2})*[^\"]*$)";
    private static final String AND_OPERATOR = " AND ";
    private static final Pattern EQUALITY_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(=)\\s*?(\".*\")\\s*?");

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            String[] andConditions = rawShowConditionString.split(AND_CONDITION_REGEX);
            Optional<ShowCondition> optShowCondition = buildShowCondition(andConditions);
            if (optShowCondition.isPresent()) {
                return optShowCondition.get();
            }
        } catch (Exception e) {
            LOG.error("Error occurred while parsing show condition", e);
            // Do nothing; we're throwing InvalidShowConditionException below
        }
        throw new InvalidShowConditionException(rawShowConditionString);
    }

    private Optional<ShowCondition> buildShowCondition(String[] andConditions) {
        ShowCondition.Builder showConditionBuilder = new ShowCondition.Builder();
        String andOperator = "";

        for (String andCondition : andConditions) {
            Matcher matcher = EQUALITY_CONDITION_PATTERN.matcher(andCondition);
            if (matcher.find()) {
                showConditionBuilder
                    .showConditionExpression(andOperator + parseEqualityCondition(matcher))
                    .field(getLeftHandSideOfEquals(matcher));
                andOperator = AND_OPERATOR;
            } else {
                return Optional.empty();
            }
        }
        if (showConditionBuilder.hasShowCondition()) {
            return Optional.of(showConditionBuilder.build());
        }
        return Optional.empty();
    }

    private String parseEqualityCondition(Matcher matcher) {
        return getLeftHandSideOfEquals(matcher) + getEqualsSign(matcher) + getRightHandSideOfEquals(matcher);
    }

    private String getLeftHandSideOfEquals(Matcher matcher) {
        return matcher.group(1).trim();
    }

    private String getEqualsSign(Matcher matcher) {
        return matcher.group(2).trim();
    }

    private String getRightHandSideOfEquals(Matcher matcher) {
        return matcher.group(3).trim();
    }
}
