package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrudValidatorTest {

    private CrudValidator validator = new CrudValidator();

    @Test
    public void validateBlankCrud() {
        final boolean result = validator.isValidCrud("   ");
        assertThat(result, is(false));
    }

    @Test
    public void validateLongCrud() {
        final boolean result = validator.isValidCrud("CRUDcr");
        assertThat(result, is(false));
    }

    @Test
    public void validateNullCrud() {
        final boolean result = validator.isValidCrud(null);
        assertThat(result, is(false));
    }

    @Test
    public void validateInvalidCrud() {
        final boolean result = validator.isValidCrud("xcr");
        assertThat(result, is(false));
    }

    @Test
    public void shouldHaveEmptyResult_whenValidCrud() {
        final boolean result = validator.isValidCrud("cr U ");
        assertThat(result, is(true));
    }
}
