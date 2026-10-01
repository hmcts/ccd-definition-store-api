package uk.gov.hmcts.ccd.definition.store.elastic;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorResponse;

public final class ElasticsearchExceptionFactory {

    private ElasticsearchExceptionFactory() {
    }

    public static ElasticsearchException elasticsearchException(String type, String reason) {
        ErrorResponse response = ErrorResponse.of(builder -> builder
            .status(400)
            .error(error -> error.type(type).reason(reason)));
        return new ElasticsearchException("test", response);
    }
}
