package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;

import java.util.regex.Matcher;

@Component
public class MaxFieldLimitErrorMessageBuilder extends ElasticsearchErrorMessageBuilder {

    private static final String REASON_PATTERN =
        "Limit of total fields \\[(.+)\\] in index \\[(.+)\\] has been exceeded";

    public MaxFieldLimitErrorMessageBuilder() {
        super(REASON_PATTERN);
    }

    @Override
    protected String buildMessage(ElasticsearchError error) {
        Matcher m = getReasonMatcher(error.getReason());
        return String.format("Case type '%s' exceeds the limit of %s fields permitted by Elasticsearch. "
                + "To reduce the number, please configure fields for this case type that are not required "
                + "to be searched on to be non-searchable in the definition.",
            error.getCaseTypeReference(),
            m.group(1));
    }
}
