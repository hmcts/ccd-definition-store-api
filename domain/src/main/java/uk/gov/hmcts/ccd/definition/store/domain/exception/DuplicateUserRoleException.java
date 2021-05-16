package uk.gov.hmcts.ccd.definition.store.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * RDM-10539 - The UserRoles will not be changed in this class as it is only used in the UserRole controller which will
 * soon possibly be out of commission in the future
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DuplicateUserRoleException extends RuntimeException {

    public DuplicateUserRoleException(String message) {
        super(message);
    }

}
