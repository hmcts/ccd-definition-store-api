package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrudValidatorTest {

    @Test
    public void validateBlankCrud() {
        final boolean result = CrudValidator.isValidCrud("   ");
        assertThat(result, is(false));
    }

    @Test
    public void validateLongCrud() {
        final boolean result = CrudValidator.isValidCrud("CRUDcr");
        assertThat(result, is(false));
    }

    @Test
    public void validateNullCrud() {
        final boolean result = CrudValidator.isValidCrud(null);
        assertThat(result, is(false));
    }

    @Test
    public void validateInvalidCrud() {
        final boolean result = CrudValidator.isValidCrud("xcr");
        assertThat(result, is(false));
    }

    @Test
    public void shouldHaveEmptyResult_whenValidCrud() {
        final boolean result = CrudValidator.isValidCrud("cr U ");
        assertThat(result, is(true));
    }
}
