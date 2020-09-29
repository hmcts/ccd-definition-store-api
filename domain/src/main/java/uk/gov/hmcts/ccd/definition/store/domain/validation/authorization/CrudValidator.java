package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class CrudValidator {

    private CrudValidator() {
    }

    private static final Pattern CRUD_PATTERN = Pattern.compile("^[CRUDcrud\\s]{1,5}$");

    public static boolean isValidCrud(final String crud) {
        return CRUD_PATTERN.matcher(trimToEmpty(crud)).matches();
    }
}
