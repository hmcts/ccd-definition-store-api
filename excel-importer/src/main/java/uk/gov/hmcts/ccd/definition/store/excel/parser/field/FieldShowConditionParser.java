package uk.gov.hmcts.ccd.definition.store.excel.parser.field;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;

public interface FieldShowConditionParser {

    ShowConditionParser getShowConditionParser();

    default String parseShowCondition(String showCondition) {
        String parsedShowCondition = showCondition;
        try {
            // Try to parse from 'spreadsheet format'...
            parsedShowCondition = getShowConditionParser().parseShowCondition(showCondition).getShowConditionExpression();
        } catch (InvalidShowConditionException e) {
            // ...and if we fail leave as the 'spreadsheet format' to let validation fall over
        }
        return parsedShowCondition;

    }
}
