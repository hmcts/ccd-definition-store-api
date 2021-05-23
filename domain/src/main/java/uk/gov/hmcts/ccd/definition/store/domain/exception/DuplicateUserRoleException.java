package uk.gov.hmcts.ccd.definition.store.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DuplicateUserRoleException extends RuntimeException {

    public DuplicateUserRoleException(String message) {
        super(message);
    }

}
