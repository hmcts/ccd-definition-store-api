package uk.gov.hmcts.ccd.definition.store.domain.service.display;

class MissingEventCaseFieldEntityException extends RuntimeException {

    MissingEventCaseFieldEntityException(String message) {
        super(message);
    }
}
