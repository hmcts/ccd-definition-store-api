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
    private static final String OR_CONDITION_REGEX = "\\sOR\\s(?=(([^\"]*\"){2})*[^\"]*$)";
    private static final String OR_OPERATOR = " OR ";
    private static final Pattern EQUALITY_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\")\\s*?");
    private static final Pattern NOT_EQUAL_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(!=|CONTAINS)\\s*?(\""
        + ".*\")\\s*?");
    private Pattern orConditionPattern = Pattern.compile(OR_CONDITION_REGEX);

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            if (rawShowConditionString != null) {
                String conditionalOperator = AND_OPERATOR;
                String[] conditions;
                Matcher matcher = orConditionPattern.matcher(rawShowConditionString);
                if (matcher.find()) {
                    conditions = rawShowConditionString.split(OR_CONDITION_REGEX);
                    conditionalOperator = OR_OPERATOR;
                } else {
                    conditions = rawShowConditionString.split(AND_CONDITION_REGEX);
                }
                Optional<ShowCondition> optShowCondition = buildShowCondition(conditions, conditionalOperator);
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

    private Optional<ShowCondition> buildShowCondition(String[] conditions, String conditionalOperator) {
        ShowCondition.Builder showConditionBuilder = new ShowCondition.Builder();
        String operator = "";

        for (String condition : conditions) {
            Matcher equalityMatcher = EQUALITY_CONDITION_PATTERN.matcher(condition);
            Matcher notEqualityMatcher = NOT_EQUAL_CONDITION_PATTERN.matcher(condition);
            if (notEqualityMatcher.find()) {
                operator = buildShowCondition(conditionalOperator, showConditionBuilder, operator, notEqualityMatcher);
            } else if (equalityMatcher.find()) {
                operator = buildShowCondition(conditionalOperator, showConditionBuilder, operator, equalityMatcher);
            } else {
                return Optional.empty();
            }
        }
        if (showConditionBuilder.hasShowCondition()) {
            return Optional.of(showConditionBuilder.build());
        }
        return Optional.empty();
    }

    private String buildShowCondition(final String conditionalOperator, final ShowCondition.Builder showConditionBuilder,
                                      String operator, final Matcher equalityMatcher) {
        showConditionBuilder
                .showConditionExpression(operator + parseEqualityCondition(equalityMatcher))
                .field(getLeftHandSideOfEquals(equalityMatcher));
        operator = conditionalOperator;
        return operator;
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
