package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class CrudValidatorTest {

    @Test
    void validateBlankCrud() {
        final boolean result = CrudValidator.isValidCrud("   ");
        assertThat(result, is(false));
    }

    @Test
    void validateLongCrud() {
        final boolean result = CrudValidator.isValidCrud("CRUDcr");
        assertThat(result, is(false));
    }

    @Test
    void validateNullCrud() {
        final boolean result = CrudValidator.isValidCrud(null);
        assertThat(result, is(false));
    }

    @Test
    void validateInvalidCrud() {
        final boolean result = CrudValidator.isValidCrud("xcr");
        assertThat(result, is(false));
    }

    @Test
    void shouldHaveEmptyResult_whenValidCrud() {
        final boolean result = CrudValidator.isValidCrud("cr U ");
        assertThat(result, is(true));
    }
}
