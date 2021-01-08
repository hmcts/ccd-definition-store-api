package uk.gov.hmcts.ccd.definition.store.elastic.exception.handler;

import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticsearchError;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorMessageBuilder;

public class TestElasticsearchErrorMessageBuilder extends ElasticsearchErrorMessageBuilder {

    private final String message;

    public TestElasticsearchErrorMessageBuilder(String reasonPattern, String message) {
        super(reasonPattern);
        this.message = message;
    }

    @Override
    protected String buildMessage(ElasticsearchError error) {
        return message;
    }
}
