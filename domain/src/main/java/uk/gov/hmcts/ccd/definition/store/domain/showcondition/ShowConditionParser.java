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

    private static final String BRACKET_REGEX = "[\\(|\\)]";
    private static final String AND_CONDITION_REGEX = "[\\sAND\\s(?=(([^\"]*\"){2})*[^\"]*$)]";
    private static final String AND_OPERATOR = " AND ";
    private static final String OR_CONDITION_REGEX = "[\\sOR\\s(?=(([^\"]*\"){2})*[^\"]*$)]";
    private static final String BOTH_CONDITION_REGEX = "[\\s(OR|AND)\\s(?=(([^\"]*\"){2})*[^\"]*$)]";
    private static final String BOTH_CONDITION_REGEX_SPLIT =
        "[((?=(\\s(OR|AND)\\s(?=(([^\"]*\"){2})*[^\"]*$)))|(?<=(\\s(OR|AND)\\s(?=(([^\"]*\"){2})*[^\"]*$))))]";
    private static final String OR_OPERATOR = " OR ";
    private static final Pattern EQUALITY_CONDITION_PATTERN_WITH_TRAILING_BRACKET = Pattern.compile(
        "\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\"\\)*)\\s*?");
    private static final Pattern NOT_EQUAL_CONDITION_PATTERN = Pattern.compile(
        "\\s*?(.*)\\s*?(!=|CONTAINS)\\s*?(\".*\")\\s*?");
    private static final Pattern EQUALITY_CONDITION_PATTERN_WITHOUT_TRAILING_BRACKET = Pattern.compile(
        "\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\")\\s*?");
    private static final String INCORRECT_POSITION_REGEX = "\\(=|=\\(|\\)=|=\\)|AND\\)|OR\\)";
    private final Pattern orConditionPattern = Pattern.compile(OR_CONDITION_REGEX);
    private final Pattern andConditionPattern = Pattern.compile(AND_CONDITION_REGEX);
    private final Pattern bothConditionPattern = Pattern.compile(BOTH_CONDITION_REGEX);
    private final Pattern incorrectPoistionPattern = Pattern.compile(INCORRECT_POSITION_REGEX);

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            if (rawShowConditionString != null) {

                boolean containsAnd = containsCondition(andConditionPattern.matcher(rawShowConditionString));
                boolean containsOr = containsCondition(orConditionPattern.matcher(rawShowConditionString));
                String conditionalOperator = (containsAnd && !containsOr) ? AND_OPERATOR : OR_OPERATOR;
                List<String> conditionalOperatorList = new ArrayList<>();
                String[] conditions = getConditions(rawShowConditionString,
                    containsAnd, containsOr, conditionalOperatorList);
                validateParenthesis(rawShowConditionString, conditions);
                Optional<ShowCondition> optShowCondition =
                    buildShowCondition(conditions, conditionalOperator, conditionalOperatorList);
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

    private String[] getConditions(String rawShowConditionString,
                                   boolean containsAnd, boolean containsOr, List<String> conditionalOperatorList) {
        String[] conditions;
        if (containsAnd && containsOr) {
            conditions = rawShowConditionString.split(BOTH_CONDITION_REGEX_SPLIT);
            List<String> values = Arrays.asList(conditions);
            for (String value : values) {
                if (bothConditionPattern.matcher(value).find()) {
                    conditionalOperatorList.add(value);
                    conditions = ArrayUtils.removeElement(conditions, value);
                }
            }
        } else if (containsOr) {
            conditions = rawShowConditionString.split(OR_CONDITION_REGEX);
        } else if (containsAnd) {
            conditions = rawShowConditionString.split(AND_CONDITION_REGEX);
        } else {
            conditions = rawShowConditionString.split(AND_CONDITION_REGEX);
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
                                                       String conditionalOperator,
                                                       List<String> conditionalOperatorList) {
        ShowCondition.Builder showConditionBuilder = new ShowCondition.Builder();
        String operator = "";
        for (String condition : conditions) {
            if (!conditionalOperatorList.isEmpty()) {
                int index = Arrays.asList(conditions).indexOf(condition);
                if (index <= (conditionalOperatorList.size() - 1)) {
                    conditionalOperator = conditionalOperatorList.get(Arrays.asList(conditions).indexOf(condition));
                } else {
                    conditionalOperator = AND_OPERATOR;
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
        if (rawConditionString.contains("(") || rawConditionString.contains(")")) {
            rawConditionString = rawConditionString.replace(" ", "");
            incorrectPosition(rawConditionString);
            checkForMismatchInBrackets(rawConditionString, conditions);
        }
    }

    private void checkForMismatchInBrackets(String rawConditionString, String[] conditions)
        throws InvalidShowConditionException {
        long openBracketCounter = rawConditionString.codePoints().filter(ch -> ch == '(').count();
        long closeBracketCounter = rawConditionString.codePoints().filter(ch -> ch == ')').count();
        for (String string : conditions) {
            Matcher equalityMatcher = EQUALITY_CONDITION_PATTERN_WITHOUT_TRAILING_BRACKET.matcher(string);
            Matcher notEqualityMatcher = NOT_EQUAL_CONDITION_PATTERN.matcher(string);
            String value = "";
            if (notEqualityMatcher.find()) {
                value = getRightHandSideOfEquals(notEqualityMatcher);
            } else if (equalityMatcher.find()) {
                value = getRightHandSideOfEquals(equalityMatcher).trim();
            }
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == '(') {
                    openBracketCounter--;
                } else if (value.charAt(i) == ')') {
                    closeBracketCounter--;
                }
            }
        }
        if (openBracketCounter != closeBracketCounter) {
            throw new InvalidShowConditionException(rawConditionString);
        }
    }

    private void incorrectPosition(String rawConditionString) throws InvalidShowConditionException {
        boolean incorrectPosition = containsCondition(incorrectPoistionPattern.matcher(rawConditionString));
        if (incorrectPosition) {
            throw new InvalidShowConditionException(rawConditionString);
        }
    }
}
