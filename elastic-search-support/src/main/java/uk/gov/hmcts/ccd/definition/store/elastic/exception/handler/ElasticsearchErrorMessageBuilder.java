package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ElasticsearchErrorMessageBuilder {

    private final String reasonPattern;

    public ElasticsearchErrorMessageBuilder(String reasonPattern) {
        this.reasonPattern = reasonPattern;
    }

    public final String doBuildMessage(ElasticsearchError error) {
        if (!canHandleError(error)) {
            throw new IllegalArgumentException(
                String.format("This error message builder does not support errors with the provided reason '%s'.",
                    error.getReason()));
        }

        return buildMessage(error);
    }

    protected abstract String buildMessage(ElasticsearchError error);

    public final boolean canHandleError(ElasticsearchError error) {
        return error.getReason().matches(reasonPattern);
    }

    protected Matcher getReasonMatcher(String reason) {
        Matcher m = Pattern.compile(reasonPattern).matcher(reason);
        m.matches();
        return m;
    }
}
