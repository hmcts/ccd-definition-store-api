package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

public class EventPostStateParser {

    private static final String POST_STATE_SEPARATOR = ";";

    private static final String PRIORITY_SEPARATOR = ":";

    private static final String WILDCARD = "*";

    private static final int DEFAULT_PRIORITY = 99;

    private static final String POST_STATE_CONDITION_PATTERN = ".*[\\(.*\\)]:\\d{1,2}";

    private static final String REPLACE_CHAR_PATTERN = "[\\(\\)]";

    private final String caseTypeId;

    private final ParseContext parseContext;

    private final ShowConditionParser conditionParser;

    public EventPostStateParser(ParseContext parseContext,
                                String caseTypeId) {
        this.caseTypeId = caseTypeId;
        this.parseContext = parseContext;
        this.conditionParser = new ShowConditionParser();
    }

    public List<EventPostStateEntity> parse(String postStateEntry) {
        if (StringUtils.isBlank(postStateEntry)) {
            return Lists.newArrayList();
        }

        String[] postStateEntries = postStateEntry.split(POST_STATE_SEPARATOR);
        return Arrays.stream(postStateEntries)
            .map(entry -> {
                if (isDefaultState(entry)) {
                    EventPostStateEntity postStateEntity = new EventPostStateEntity();
                    setPostStateReference(postStateEntity, entry);
                    postStateEntity.setPriority(DEFAULT_PRIORITY);
                    return postStateEntity;
                }
                return createPostStateEntity(entry);
            }).collect(Collectors.toList());
    }

    private EventPostStateEntity createPostStateEntity(String entry) {
        if (entry.matches(POST_STATE_CONDITION_PATTERN)) {
            String[] postStateWithPriority = entry.split(PRIORITY_SEPARATOR);
            String postSate = postStateWithPriority[0];
            int priority = Integer.parseInt(postStateWithPriority[1]);
            EventPostStateEntity postStateEntity = new EventPostStateEntity();
            postStateEntity.setEnablingCondition(getParsedCondition(postSate));
            postStateEntity.setPriority(priority);
            setPostStateReference(postStateEntity, postSate.substring(0, postSate.indexOf("(")));
            return postStateEntity;
        }
        throw new SpreadsheetParsingException("Invalid Post State " + entry);
    }

    private void setPostStateReference(EventPostStateEntity entity, String reference) {
        if (reference.equalsIgnoreCase(WILDCARD)) {
            entity.setPostStateReference(reference);
        } else {
            StateEntity stateEntity = parseContext.getStateForCaseType(this.caseTypeId, reference);
            if (stateEntity != null) {
                entity.setPostStateReference(reference);
            }
        }
    }

    private String extractPostStateCondition(final String postSate) {
        return postSate.substring(postSate.indexOf("("))
            .replaceAll(REPLACE_CHAR_PATTERN, "");
    }

    private String getParsedCondition(final String postSate) {
        try {
            String postStateCondition = extractPostStateCondition(postSate);
            ShowCondition condition = this.conditionParser.parseShowCondition(postStateCondition);
            return condition.getShowConditionExpression();
        } catch (InvalidShowConditionException e) {
            throw new SpreadsheetParsingException("Invalid Post State Condition " + postSate);
        }
    }

    private boolean isDefaultState(final String conditionEntry) {
        return !conditionEntry.contains("(");
    }
}
