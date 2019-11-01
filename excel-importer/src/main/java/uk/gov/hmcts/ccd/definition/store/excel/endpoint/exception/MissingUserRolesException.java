package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception thrown when user roles defined in Case Definition file are missing.
 */
public class MissingUserRolesException extends RuntimeException {

    public MissingUserRolesException(Set<String> missingUserRoles) {
        super(new StringBuilder("Missing UserRoles.\n\n")
            .append(missingUserRoles
                .stream()
                .collect(Collectors.joining("\n"))).toString());
    }

}
