package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FieldTypeUtilsTest {

    @Test
    public void isList() {
        assertThat(FieldTypeUtils.isList("FixedList"), is(true));
        assertThat(FieldTypeUtils.isList("MultiSelectList"), is(true));
        assertThat(FieldTypeUtils.isList("fixedlist"), is(false));
        assertThat(FieldTypeUtils.isList("multiselectlist"), is(false));
    }
}
