package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShowConditionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ShowConditionParser.class);

    private static final String AND_CONDITION_REGEX = "\\sAND\\s(?=(([^\"]*\"){2})*[^\"]*$)";
    private static final String AND_OPERATOR = " AND ";
    private static final Pattern EQUALITY_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\")\\s*?");

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            if (rawShowConditionString != null) {
                String[] andConditions = rawShowConditionString.split(AND_CONDITION_REGEX);
                Optional<ShowCondition> optShowCondition = buildShowCondition(andConditions);
                if (optShowCondition.isPresent()) {
                    ShowCondition showCondition = optShowCondition.get();
                    if (showCondition.getFieldsWithSubtypes().stream().noneMatch(this::fieldContainsEmpties)
                        && showCondition.getFields().stream().noneMatch(this::fieldContainsEmpties)) {
                        return showCondition;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error occurred while parsing show condition", e);
            // Do nothing; we're throwing InvalidShowConditionException below
        }
        throw new InvalidShowConditionException(rawShowConditionString);
    }

    private boolean fieldContainsEmpties(String field) {
        return field.contains(" ") || field.contains("..");
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
