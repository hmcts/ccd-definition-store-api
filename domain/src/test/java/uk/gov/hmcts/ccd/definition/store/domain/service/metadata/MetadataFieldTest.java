package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

class MetadataFieldTest {

    @Test
    @DisplayName("Should return list of dynamic metadata fields")
    void shouldReturnListOfDynamicMetadataFields() {
        final List<MetadataField> dynamicFields = MetadataField.getDynamicFields();

        assertEquals(1, dynamicFields.size());
        assertThat(dynamicFields, hasItem(MetadataField.STATE));
    }

    @Test
    @DisplayName("Should return true when field reference is a metadata field")
    void shouldReturnTrueWhenFieldReferenceIsAMetadataField() {
        final boolean isMetadataField = MetadataField.isMetadataField("[STATE]");

        assertTrue(isMetadataField);
    }

    @Test
    @DisplayName("Should return false when field reference is not a metadata field")
    void shouldReturnFalseWhenFieldReferenceIsNotAMetadataField() {
        final boolean isMetadataField = MetadataField.isMetadataField("STATE");

        assertFalse(isMetadataField);
    }
}
