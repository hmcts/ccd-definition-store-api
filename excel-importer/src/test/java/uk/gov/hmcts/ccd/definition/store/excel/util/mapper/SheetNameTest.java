package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class SheetNameTest {

    @DisplayName("Should return null when Sheet name is not found")
    @Test
    void shouldReturnNullWhenSheetNameIsNotFound() {
        assertNull(SheetName.forName("SheetNameNotExpectHere"));
    }

    @DisplayName("Should return a sheet when matching name is found")
    @Test
    void shouldReturnSheetWhenMatchingNameIsFound() {
        final SheetName[] sheetNames = SheetName.values();
        for (SheetName s : sheetNames) {
            assertThat(SheetName.forName(s.getName()), is(s));
        }
    }
}
