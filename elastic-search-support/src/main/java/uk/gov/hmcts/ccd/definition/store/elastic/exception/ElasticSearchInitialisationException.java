package uk.gov.hmcts.ccd.definition.store.elastic.exception;

public class ElasticSearchInitialisationException extends RuntimeException {

    public ElasticSearchInitialisationException(Throwable cause) {
        super(cause);
    }

    public ElasticSearchInitialisationException(String message) {
        super(message);
    }

    public ElasticSearchInitialisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
