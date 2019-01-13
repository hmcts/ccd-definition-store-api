package uk.gov.hmcts.ccd.definition.store.domain.service.display;

class MissingDisplayContextException extends RuntimeException {

    MissingDisplayContextException(String message) {
        super(message);
    }
}
