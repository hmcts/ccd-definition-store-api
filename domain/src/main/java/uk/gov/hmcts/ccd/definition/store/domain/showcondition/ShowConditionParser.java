package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ShowConditionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ShowConditionParser.class);

    private static final String BRACKET_REGEX = "[()]";
    private static final char LEFT_BRACKET = '(';
    private static final char RIGHT_BRACKET = ')';
    @SuppressWarnings("squid:S5998")
    private static final String BOTH_CONDITION_REGEX = "\\s(OR|AND)\\s(?=(([^\"]*\"){2})*[^\"]*$)";
    private static final String BOTH_CONDITION_REGEX_SPLIT =
        "((?=(" + BOTH_CONDITION_REGEX + ")))|(?<=(" + BOTH_CONDITION_REGEX + "))";
    @SuppressWarnings("squid:S5852")
    private static final Pattern EQUALITY_CONDITION_PATTERN_WITH_TRAILING_BRACKET = Pattern.compile(
        "\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\"\\)*)\\s*?");
    @SuppressWarnings("squid:S5852")
    private static final Pattern NOT_EQUAL_CONDITION_PATTERN = Pattern.compile(
        "\\s*?(.*)\\s*?(!=|CONTAINS)\\s*?(\".*\")\\s*?");
    @SuppressWarnings("squid:S5852")
    private static final Pattern EQUALITY_CONDITION_PATTERN_WITHOUT_TRAILING_BRACKET = Pattern.compile(
        "\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\")\\s*?");
    private static final String INCORRECT_POSITION_OF_PARENTHESIS_REGEX = "\\(=|=\\(|\\)=|=\\)|AND\\)|OR\\)";
    private static final String INCORRECT_POSITION_OF_CONDITION_REGEX = "^\\s?(AND|OR)\\s?";

    private static final Pattern BRACKET_PATTERN = Pattern.compile(BRACKET_REGEX);
    private static final Pattern BOTH_CONDITION_PATTERN = Pattern.compile(BOTH_CONDITION_REGEX);
    private static final Pattern INCORRECT_POSITION_PATTERN = Pattern.compile(INCORRECT_POSITION_OF_PARENTHESIS_REGEX);
    private static final Pattern INCORRECT_POSITION_CONDITION_PATTERN =
        Pattern.compile(INCORRECT_POSITION_OF_CONDITION_REGEX);

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            if (rawShowConditionString != null) {
                validateCorrectPosition(rawShowConditionString, INCORRECT_POSITION_CONDITION_PATTERN);
                List<String> conditionalOperatorList = new ArrayList<>();
                String[] conditions = getConditions(rawShowConditionString, conditionalOperatorList);
                validateParenthesis(rawShowConditionString, conditions);
                Optional<ShowCondition> optShowCondition =
                    buildShowCondition(conditions, conditionalOperatorList);
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

    private String[] getConditions(String rawShowConditionString, List<String> conditionalOperatorList) {
        String[] conditions = rawShowConditionString.split(BOTH_CONDITION_REGEX_SPLIT);
        for (String condition : conditions) {
            if (BOTH_CONDITION_PATTERN.matcher(condition).find()) {
                conditionalOperatorList.add(condition);
                conditions = ArrayUtils.removeElement(conditions, condition);
            }
        }
        return conditions;
    }

    private boolean containsCondition(Matcher matcher) {
        return matcher.find();
    }

    private boolean fieldContainsEmpties(String field) {
        return field.contains(" ") || field.contains("..");
    }

    private Optional<ShowCondition> buildShowCondition(String[] conditions,
                                                       List<String> conditionalOperatorList) {
        ShowCondition.Builder showConditionBuilder = new ShowCondition.Builder();
        String operator = "";
        String conditionalOperator = "";
        for (String condition : conditions) {
            if (!conditionalOperatorList.isEmpty()) {
                int index = Arrays.asList(conditions).indexOf(condition);
                if (index <= (conditionalOperatorList.size() - 1)) {
                    conditionalOperator = conditionalOperatorList.get(Arrays.asList(conditions).indexOf(condition));
                }
            }
            Matcher equalityMatcher = EQUALITY_CONDITION_PATTERN_WITH_TRAILING_BRACKET.matcher(condition);
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

    private String buildShowCondition(final String conditionalOperator,
                                      final ShowCondition.Builder showConditionBuilder,
                                      String operator, final Matcher equalityMatcher) {
        showConditionBuilder
            .showConditionExpression(operator + parseEqualityCondition(equalityMatcher))
            .field(getLeftHandSideOfEquals(equalityMatcher).replaceAll(BRACKET_REGEX, ""));
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
        return matcher.group(3);
    }

    private void validateParenthesis(String rawConditionString, String[] conditions)
        throws InvalidShowConditionException {
        if (containsCondition(BRACKET_PATTERN.matcher(rawConditionString))) {
            rawConditionString = rawConditionString.replace(" ", "");
            validateCorrectPosition(rawConditionString, INCORRECT_POSITION_PATTERN);
            checkForMismatchInBrackets(rawConditionString, conditions);
        }
    }

    private void checkForMismatchInBrackets(String rawConditionString, String[] conditions)
        throws InvalidShowConditionException {
        long openBracketCounter = rawConditionString.codePoints().filter(ch -> ch == LEFT_BRACKET).count();
        long closeBracketCounter = rawConditionString.codePoints().filter(ch -> ch == RIGHT_BRACKET).count();
        for (String string : conditions) {
            Matcher equalityMatcher = EQUALITY_CONDITION_PATTERN_WITHOUT_TRAILING_BRACKET.matcher(string);
            String value = "";
            if (equalityMatcher.find()) {
                value = getRightHandSideOfEquals(equalityMatcher).trim();
            }
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == LEFT_BRACKET) {
                    openBracketCounter--;
                } else if (value.charAt(i) == RIGHT_BRACKET) {
                    closeBracketCounter--;
                }
            }
        }
        if (openBracketCounter != closeBracketCounter) {
            throw new InvalidShowConditionException(rawConditionString);
        }
    }

    private void validateCorrectPosition(String rawConditionString, Pattern pattern)
        throws InvalidShowConditionException {
        if (containsCondition(pattern.matcher(rawConditionString))) {
            throw new InvalidShowConditionException(rawConditionString);
        }
    }
}
