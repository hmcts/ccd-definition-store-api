package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FieldTypeUtilsTest {

    @Test
    void isList() {
        assertThat(FieldTypeUtils.isList("FixedList"), is(true));
        assertThat(FieldTypeUtils.isList("MultiSelectList"), is(true));
        assertThat(FieldTypeUtils.isList("fixedlist"), is(false));
        assertThat(FieldTypeUtils.isList("multiselectlist"), is(false));
    }
}
