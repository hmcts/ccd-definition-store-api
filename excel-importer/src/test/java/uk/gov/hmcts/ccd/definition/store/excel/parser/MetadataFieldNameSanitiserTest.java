package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MetadataFieldNameSanitiserTest {

    @Test
    @DisplayName("should sanitise metadata field names in a label")
    void shouldSanitiseMetadataFieldNameInString() {
        String label = "This is metadata ${[STATE]} with ${[CREATED_DATE]} and ${[STATE]} again";
        String sanitised = MetadataFieldNameSanitiser.sanitiseMetadataFieldNameInString(label);

        assertThat(sanitised, is("This is metadata ${state} with ${created_date} and ${state} again"));
    }

    @Test
    @DisplayName("should return label when no metadata field names in the label")
    void shouldReturnLabelWhenNoMetadataFields() {
        String label = "case data field ${field}";
        String sanitised = MetadataFieldNameSanitiser.sanitiseMetadataFieldNameInString(label);

        assertThat(sanitised, is(label));
    }

    @Test
    @DisplayName("should construct metadata field name from origina name")
    void shouldConstructMetadataFieldName() {
        String originalFieldName = "state";
        String metadataFieldName = MetadataFieldNameSanitiser.constructMetadataFieldName(originalFieldName);

        assertThat(metadataFieldName, is("[STATE]"));
    }
}
